package net.coderbot.iris.parsing;

import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.function.TypedFunction;

import java.util.Collection;

public class VectorizedFunction implements TypedFunction {
	final TypedFunction inner;
	final int size;
	final VectorType.ArrayVector returnType;
	final VectorType.ArrayVector[] parameterTypes;
	
	private final ElementAccessExpression[] vectorAccessors;
	private int index;
	
	public VectorizedFunction(TypedFunction inner, int size) {
		this.inner = inner;
		this.size = size;
		
		this.returnType = new VectorType.ArrayVector(inner.getReturnType(), size);
		
		Type[] innerTypes = inner.getParameterTypes();
		this.parameterTypes = new VectorType.ArrayVector[innerTypes.length];
		this.vectorAccessors = new ElementAccessExpression[innerTypes.length];
		
		for (int i = 0; i < innerTypes.length; i++) {
			this.parameterTypes[i] = new VectorType.ArrayVector(innerTypes[i], size);
			this.vectorAccessors[i] = new ElementAccessExpression(innerTypes[i]);
		}
	}
	
	@Override
	public Type getReturnType() {
		return this.returnType;
	}
	
	@Override
	public Type[] getParameterTypes() {
		return this.parameterTypes;
	}
	
	@Override
	public void evaluateTo(Expression[] params, FunctionContext context, FunctionReturn functionReturn) {
		for (int p = 0; p < params.length; p++) {
			final Expression param = params[p];
			param.evaluateTo(context, functionReturn);
			this.vectorAccessors[p].vector = functionReturn.objectReturn;
		}
		
		returnType.map(this, context, functionReturn, this.mapper);
	}
	
	private final VectorType.ArrayVector.IntObjectObjectObjectConsumer<VectorizedFunction, FunctionContext, FunctionReturn> mapper =
			(i, self, ctx, fr) -> {
				self.index = i;
				self.inner.evaluateTo(self.vectorAccessors, ctx, fr);
			};
	
	class ElementAccessExpression implements Expression {
		final Type parameterType;
		Object vector;
		
		
		ElementAccessExpression(Type parameterType) {
			this.parameterType = parameterType;
		}
		
		@Override
		public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
			parameterType.getValueFromArray(vector, index, functionReturn);
		}
		
		
		@Override
		public void listVariables(Collection<? super VariableExpression> variables) {
			throw new IllegalStateException();
		}
	}
}
