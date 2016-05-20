package nxt.execution;
import java.io.InputStream;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.BaseLib;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import java.util.Scanner;
import org.luaj.vm2.Globals;
import org.luaj.vm2.InstructionLimit;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;


public class PowLogic {
	private static int	lastMemory;
	
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
	
	public PowLogic(){
		// set limits here
		final InstructionLimit initializerLimit = new InstructionLimit();
		initializerLimit.setMaxInstructions(50);
		InstructionLimit.instructionLimit(initializerLimit);
	}
	
	public void test() throws InterruptedException{
		
		Globals globals = standardGlobals();
		
		String testScript = "";
		testScript=testScript+"local tickcount = 0;"+"\n";
		testScript=testScript+"  leak = {}"+"\n";
		testScript=testScript+"function tick()"+"\n";
		testScript=testScript+"  while tickcount<100 do"+"\n";
		testScript=testScript+"    tickcount = tickcount +1;"+"\n";
		testScript=testScript+"    local string = \"inner loop \" .. tickcount;"+"\n";
		testScript=testScript+"    if tickcount % 100 == 0 then"+"\n";
		testScript=testScript+"      leak[tickcount]=string"+"\n";
		testScript=testScript+"    end"+"\n";
		testScript=testScript+"    if tickcount % 1000 == 0 then"+"\n";
		testScript=testScript+"      leak = {}"+"\n";
		testScript=testScript+"    end"+"\n";
		testScript=testScript+"  end"+"\n";
		testScript=testScript+"end"+"\n";
		
		
		final LuaClosure chunk = (LuaClosure) globals.load(testScript.toString(), 500);

		final InstructionLimit initializerLimit = new InstructionLimit();
		initializerLimit.setMaxInstructions(50);
		InstructionLimit.instructionLimit(initializerLimit);
		chunk.call();

		final LuaClosure tickHook = (LuaClosure) globals.get("tick");
		final LuaThread tickWorker = new LuaThread(globals, tickHook.checkfunction());
		tickWorker.resume(LuaValue.NIL); 
		final InstructionLimit coroutineInstructionLimit = InstructionLimit.instructionLimit(tickWorker);
		coroutineInstructionLimit.setMaxInstructions(500);
		
		while (true) {
			Thread.sleep(50);
			final int mb = 1024 * 1024;
			final Runtime runtime = Runtime.getRuntime();
			final long start = System.currentTimeMillis();
			final Varargs returnValue = tickWorker.resume(LuaValue.NIL); // tick 1 is expected to be immidiatly put to sleep
			final int newMemory = globals.getUsedMemory();
			final int memoryGrowth = newMemory - this.lastMemory;
			this.lastMemory = newMemory;
			System.out.println("Used Memory " + this.lastMemory + " " + (memoryGrowth > 0 ? "+" + memoryGrowth : memoryGrowth));
			System.out.println("Took " + (System.currentTimeMillis() - start) + " Instructions " + coroutineInstructionLimit.getCurrentInstructions() + "  Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
			final LuaValue processedWithoutError = returnValue.arg(1);
			if (processedWithoutError.isboolean() && !((LuaBoolean) processedWithoutError).v) {
				System.out.println("Terminating long running Task due to error " + returnValue.arg(2));
				break;
			}
			InstructionLimit.reset(tickWorker);
		}
	}
	
	public static void main(String [] args)
	{
		PowLogic l = new PowLogic();
		try {
			l.test();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
