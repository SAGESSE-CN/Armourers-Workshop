package moe.plushie.armourers_workshop.core.skin.molang.impl;

import moe.plushie.armourers_workshop.core.skin.molang.core.Binary;
import moe.plushie.armourers_workshop.core.skin.molang.core.Compound;
import moe.plushie.armourers_workshop.core.skin.molang.core.Constant;
import moe.plushie.armourers_workshop.core.skin.molang.core.Expression;
import moe.plushie.armourers_workshop.core.skin.molang.core.Function;
import moe.plushie.armourers_workshop.core.skin.molang.core.Identifier;
import moe.plushie.armourers_workshop.core.skin.molang.core.Literal;
import moe.plushie.armourers_workshop.core.skin.molang.core.Optimized;
import moe.plushie.armourers_workshop.core.skin.molang.core.Statement;
import moe.plushie.armourers_workshop.core.skin.molang.core.Subscript;
import moe.plushie.armourers_workshop.core.skin.molang.core.Ternary;
import moe.plushie.armourers_workshop.core.skin.molang.core.Unary;
import moe.plushie.armourers_workshop.core.skin.molang.core.Variable;

/**
 * An {@link Expression} visitor. Provides a way to add
 * functionalities to the expression interface and all
 * of its implementations.
 *
 * <p>See the following example on visiting an expression:</p>
 * <pre>{@code
 *      Expression expr = ...;
 *      String str = expr.visit(new ToStringVisitor());
 * }</pre>
 *
 * @since 3.0.0
 */
public interface Visitor {

    /**
     * Evaluate for the given unknown expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visit(final Expression expression) {
        return expression;
    }

    /**
     * Evaluate for string expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitLiteral(final Literal expression) {
        return expression;
    }

    /**
     * Evaluate for identifier expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitIdentifier(final Identifier expression) {
        return expression;
    }

    /**
     * Evaluate for constant expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitConstant(final Constant expression) {
        return expression;
    }

    /**
     * Evaluate for variable expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitVariable(final Variable expression) {
        return expression;
    }

    /**
     * Evaluate for array access expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitSubscript(final Subscript expression) {
        return expression;
    }

    /**
     * Evaluate for unary expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitUnary(final Unary expression) {
        return expression;
    }

    /**
     * Evaluate for binary expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitBinary(final Binary expression) {
        return expression;
    }

    /**
     * Evaluate for ternary conditional expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitTernary(final Ternary expression) {
        return expression;
    }

    /**
     * Evaluate for call expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitFunction(final Function expression) {
        return expression;
    }

    /**
     * Evaluate for compound expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitCompound(final Compound expression) {
        return expression;
    }

    /**
     * Evaluate for statement expression.
     *
     * @param expression The expression.
     * @return The result.
     */
    default Expression visitStatement(final Statement expression) {
        return expression;
    }
}
