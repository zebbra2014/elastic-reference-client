package nxt.execution;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.luaj.vm2.Globals;
import org.luaj.vm2.InstructionLimit;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseMathLib;



public class PowLogic {
	private static int	lastMemory;
	private int numberOfInputs;
	private int numberOfOutputs;
	final InstructionLimit initializerLimit = new InstructionLimit();
	
	class FunctionsMissingException extends Exception
	{
		private static final long serialVersionUID = -7027652657501070726L;
		public FunctionsMissingException() {}
	      public FunctionsMissingException(String message)
	      {
	         super(message);
	      }
	}
	class LuaFunctionCrashedException extends Exception
	{
		private static final long serialVersionUID = -7027652657501070726L;
		public LuaFunctionCrashedException() {}
	      public LuaFunctionCrashedException(String message)
	      {
	         super(message);
	      }
	}
	class WorkFunctionMissingException extends FunctionsMissingException
	{
		private static final long serialVersionUID = -5648679344351578136L;
		public WorkFunctionMissingException() {}
	      public WorkFunctionMissingException(String message)
	      {
	         super(message);
	      }
	 }
	class BountyFunctionMissingException extends FunctionsMissingException
	{
		private static final long serialVersionUID = 8057158570147256047L;
		public BountyFunctionMissingException() {}
	      public BountyFunctionMissingException(String message)
	      {
	         super(message);
	      }
	 }
	
	class Pair<A,B> {
	  public final A output1;
	  public final B output2;
	
	  // use newPair
	  public Pair(A output1, B output2) {
	    this.output1 = output1;
	    this.output2 = output2;
	  }
	
	  @Override @SuppressWarnings("rawtypes")
	  public boolean equals(Object other) {
	    if (other == this) {
	      return true;
	    } else if (other instanceof Pair) {
	      Pair pair = (Pair) other;
	      return output1.equals(pair.output1) && output2.equals(pair.output2);
	    } else {
	      return false;
	    }
	  }
	
	  @Override
	  public int hashCode() {
	    return output1.hashCode() + output2.hashCode();
	  }
	};
	
	public BigInteger numericHash(int output[]) throws NoSuchAlgorithmException{
		// TODO!! FIXME!! Here, also hash the input variables! Generally, fix the whole thing to work like in the whitepaper
		// right now its not exactly "correct" and exploitable
		
		MessageDigest m = MessageDigest.getInstance("SHA-256");
		m.reset();
		ByteBuffer byteBuffer = ByteBuffer.allocate(output.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(output);

        byte[] array = byteBuffer.array();
		m.update(array);
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		return bigInt;
	}
	
	public BigInteger getDesiredTarget() {
		return new BigInteger("00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",16);
	}						   
	
	
	public static Globals standardGlobals() {
		final Globals globals = new Globals();
		globals.load(new BaseLib() {

			@Override
			public InputStream findResource(String filename) {
				throw new RuntimeException("Not implemented for testCases");
			}
		});
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new TableLib(globals));
		globals.load(new StringLib());
		globals.load(new JseMathLib());
		//globals.load(new RestrictedOsLib());
		//globals.load(new DebugLib());
		globals.load(new CoroutineLib());
		LuaC.install(globals);
		return globals;
	}
	
	public PowLogic(int numberOfInputs, int numberOfOutputs){
		// set limits here
		// TODO, HACKME: Not sure about those limits
		//initializerLimit.setMaxInstructions(50);
		//InstructionLimit.instructionLimit(initializerLimit);
		
		// remember number of inputs and outputs
		this.numberOfInputs = numberOfInputs;
		this.numberOfOutputs = numberOfOutputs;
	}
	
	public Pair<LuaClosure,LuaClosure> checkForFunctions(Globals globals) throws FunctionsMissingException{
		Object workObj = globals.get("work");
		Object bountyObj = globals.get("bounty");
		if (workObj.getClass()==org.luaj.vm2.LuaNil.class){
			//throw new WorkFunctionMissingException();
		}
		if (bountyObj.getClass()==org.luaj.vm2.LuaNil.class){
			//throw new BountyFunctionMissingException();
		}
		final LuaClosure workHook = (LuaClosure) workObj;
		final LuaClosure bountyHook = (LuaClosure) bountyObj;
		Pair<LuaClosure,LuaClosure> ret = new Pair<LuaClosure,LuaClosure>(workHook, bountyHook);
		return ret;
	}
	
	@SuppressWarnings("unused")
	public boolean executeBountyHooks(int inputs[], Globals globals, final LuaClosure bountyHook) throws InterruptedException, FunctionsMissingException, IOException, LuaFunctionCrashedException, NoSuchAlgorithmException{
		
		// Assets the number of inputs matches the requirement
		if (inputs.length != this.numberOfOutputs){
			throw new IllegalArgumentException("Incorrect number of inputs (must match work()'s outputs.");
		}
		
		// Now the stepwise execution loop
		final LuaThread bountyWorker = new LuaThread(globals, bountyHook.checkfunction());
		
		Varargs varargs_input = null;
		List<LuaValue> rawValues = new ArrayList<LuaValue>();
		for (int i = 0; i < inputs.length; ++i){
			rawValues.add(LuaValue.valueOf(inputs[i]));
		}
		LuaValue[] preArray = new LuaValue[inputs.length];
		rawValues.toArray(preArray);
		varargs_input = LuaValue.varargsOf(preArray);
		
		
		int steps_instruction_limit_bounty_hook = 500;
		int absolute_limit_for_bounty_function = steps_instruction_limit_bounty_hook*20;
		
		bountyWorker.resume(varargs_input); 
		final InstructionLimit coroutineInstructionLimit = InstructionLimit.instructionLimit(bountyWorker);
		coroutineInstructionLimit.setMaxInstructions(steps_instruction_limit_bounty_hook);
		
		
		
		while (true) {
			// Here, frequently check return value for "exit on error" or "work completion"
			final Varargs returnValue = bountyWorker.resume(varargs_input); // tick 1 is expected to be immidiatly put to sleep
			
			final LuaValue processedWithoutError = returnValue.arg(1);
			final LuaValue firstRealOutput = returnValue.arg(2);
			if (processedWithoutError.isboolean() && !((LuaBoolean) processedWithoutError).v) {
				throw new IllegalArgumentException("Bounty hook function seems to have crashed.");
			}
			if (processedWithoutError.isboolean() && ((LuaBoolean) processedWithoutError).v && firstRealOutput.isnil() == false) {
				// Now check if the number of outputs is exactly 1 and the output is a boolean
				// if ok, extract. Otherwise throw exception
				if(returnValue.narg()!=2){
					throw new IllegalArgumentException("Incorrect number of outputs in bounty hook function.");
				}
				if(firstRealOutput.isboolean() == false){
					throw new IllegalArgumentException("Output of bounty function is not boolean, it is a " + firstRealOutput.typename() + ".");
				}			
				return firstRealOutput.toboolean();
			}
			InstructionLimit.reset(bountyWorker);
			
			absolute_limit_for_bounty_function = absolute_limit_for_bounty_function - steps_instruction_limit_bounty_hook;
			if (absolute_limit_for_bounty_function<=0){
				// Bounty function did not execute on time, just returning false
				return false;
			}
		}
		
	}
	
	@SuppressWarnings("unused")
	public void executeProofOfWork(int inputs[]) throws InterruptedException, FunctionsMissingException, IOException, LuaFunctionCrashedException, NoSuchAlgorithmException{
		
		// Assets the number of inputs matches the requirement
		if (inputs.length != this.numberOfInputs){
			throw new IllegalArgumentException("Incorrect number of inputs.");
		}
		
		// Get Globals
		Globals globals = standardGlobals();
		
		// Load example Proof-of-Work function
		String script = new String();
		byte[] encoded = Files.readAllBytes(Paths.get("/home/anonymous/Development/elastic/src/java/nxt/execution/md5_miner_test.lua"));
		script = new String(encoded);
	
		// Initialize / Load Script into Engine
		final LuaClosure chunk = (LuaClosure) globals.load(script.toString(), 5000);
		final InstructionLimit initializerLimit = new InstructionLimit();
		initializerLimit.setMaxInstructions(100);
		InstructionLimit.reset();
		InstructionLimit.instructionLimit(initializerLimit);
		chunk.call();
		
		// Check for work/bounty function and get the LuaClosures for that
		Pair<LuaClosure, LuaClosure> retPair = checkForFunctions(globals);
		
		final LuaClosure workHook = retPair.output1;
		final LuaClosure bountyHook = retPair.output2;
		
		// Now the stepwise execution loop
		final LuaThread workWorker = new LuaThread(globals, workHook.checkfunction());
		
		Varargs varargs_input = null;
		List<LuaValue> rawValues = new ArrayList<LuaValue>();
		for (int i = 0; i < inputs.length; ++i){
			rawValues.add(LuaValue.valueOf(inputs[i]));
		}
		LuaValue[] preArray = new LuaValue[inputs.length];
		rawValues.toArray(preArray);
		varargs_input = LuaValue.varargsOf(preArray);
		
		workWorker.resume(varargs_input); 
		final InstructionLimit coroutineInstructionLimit = InstructionLimit.instructionLimit(workWorker);
		coroutineInstructionLimit.setMaxInstructions(500);
		
		while (true) {
			//Thread.sleep(50);
			final int mb = 1024 * 1024;
			final Runtime runtime = Runtime.getRuntime();
			final long start = System.currentTimeMillis();
			
			// Here, frequently check return value for "exit on error" or "work completion"
			final Varargs returnValue = workWorker.resume(varargs_input); // tick 1 is expected to be immidiatly put to sleep
			final LuaValue processedWithoutError = returnValue.arg(1);
			final LuaValue firstRealOutput = returnValue.arg(2);
			if (processedWithoutError.isboolean() && !((LuaBoolean) processedWithoutError).v) {
				throw new LuaFunctionCrashedException(returnValue.arg(2).tojstring());
			}
			if (processedWithoutError.isboolean() && ((LuaBoolean) processedWithoutError).v && firstRealOutput.isnil() == false) {
				
				// Now check if the number of outputted ints meets the requirement
				// if ok, extract. Otherwise throw exception
				if(returnValue.narg()!=this.numberOfOutputs+1){
					throw new IllegalArgumentException("Incorrect number of outputs.");
				}
				int got_output[] = new int[this.numberOfOutputs];
				for(int i=2;i<=returnValue.narg();++i){
					LuaValue output = returnValue.arg(i);
					if(output.isint() == false && output.isnumber() == false){
						throw new IllegalArgumentException("Output " + String.valueOf(i) + " is not an integer, it is a " + output.typename() + ".");
					}
					got_output[i-2]=output.toint();
				}			
				
				// Now check if we have a proof of work
				BigInteger solutionHash = this.numericHash(got_output);
				BigInteger mustBeSmallerThan = this.getDesiredTarget();
				if(solutionHash.compareTo(mustBeSmallerThan)<0){
					System.out.println("[found block] - proof of work solved: " + solutionHash.toString());
				}
				
				// Now check if we have found a bounty
				boolean isBounty = executeBountyHooks(got_output,globals,bountyHook);
				if(isBounty){
					System.out.println("[found block] - bounty solved: " + Arrays.toString(got_output));
				}
				
				
				
				break;
			}
			
			// Here, we just calculate some memory / cpu usage to display it nicely in the GUI if needed
			final int newMemory = globals.getUsedMemory();
			final int memoryGrowth = newMemory - this.lastMemory;
			this.lastMemory = newMemory;
			//System.out.println();
			//System.out.println("Last 10ms block took " + (System.currentTimeMillis() - start) + " Instructions " + coroutineInstructionLimit.getCurrentInstructions() + "  Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb + "   Last Used Memory " + this.lastMemory + " " + (memoryGrowth > 0 ? "+" + memoryGrowth : memoryGrowth));
		
			// After every 10ms block, after working off the instructions inside this loop, reset the tick counter.			
			InstructionLimit.reset(workWorker);
		}
	}
	
	
	public static void main(String [] args)
	{
		PowLogic l = new PowLogic(1, 1);
		while(true){
			try {
				
				l.executeProofOfWork(new int[]{Math.abs((new Random()).nextInt())});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
