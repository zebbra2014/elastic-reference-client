/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package org.luaj.vm2;

/**
 * Extension of {@link LuaValue} which can hold a Java boolean as its value.
 * <p>
 * These instance are not instantiated directly by clients. Instead, there are exactly twon instances of this class, {@link LuaValue#TRUE} and {@link LuaValue#FALSE} representing the lua values {@code true} and {@link false}. The function
 * {@link LuaValue#valueOf(boolean)} will always return one of these two values.
 * <p>
 * Any {@link LuaValue} can be converted to its equivalent boolean representation using {@link LuaValue#toboolean()}
 * <p>
 * 
 * @see LuaValue
 * @see LuaValue#valueOf(boolean)
 * @see LuaValue#TRUE
 * @see LuaValue#FALSE
 */
public final class LuaBoolean extends LuaValue {

	/** The singleton instance representing lua {@code true} */
	static final LuaBoolean	_TRUE	= new LuaBoolean(true);

	/** The singleton instance representing lua {@code false} */
	static final LuaBoolean	_FALSE	= new LuaBoolean(false);

	/** Shared static metatable for boolean values represented in lua. */
	public static LuaValue	s_metatable;

	/** The value of the boolean */
	public final boolean	v;

	LuaBoolean(final boolean b) {
		this.v = b;
	}

	@Override
	public int type() {
		return LuaValue.TBOOLEAN;
	}

	@Override
	public String typename() {
		return "boolean";
	}

	@Override
	public boolean isboolean() {
		return true;
	}

	@Override
	public LuaValue not() {
		return this.v ? LuaValue.FALSE : LuaValue.TRUE;
	}

	/**
	 * Return the boolean value for this boolean
	 * 
	 * @return value as a Java boolean
	 */
	public boolean booleanValue() {
		return this.v;
	}

	@Override
	public boolean toboolean() {
		return this.v;
	}

	@Override
	public String tojstring() {
		return this.v ? "true" : "false";
	}

	@Override
	public boolean optboolean(final boolean defval) {
		return this.v;
	}

	@Override
	public boolean checkboolean() {
		return this.v;
	}

	@Override
	public LuaValue getmetatable() {
		return LuaBoolean.s_metatable;
	}
}
