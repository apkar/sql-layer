/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package com.akiban.server.expression.std;

import com.akiban.server.expression.Expression;
import com.akiban.server.expression.ExpressionEvaluation;
import com.akiban.server.types.AkType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractCompositeExpression implements Expression {

    // Expression interface

    @Override
    public boolean isConstant() {
        for (Expression child : children) {
            if (!child.isConstant())
                return false;
        }
        return true;
    }

    @Override
    public boolean needsRow() {
        for (Expression child : children) {
            if (child.needsRow())
                return true;
        }
        return false;
    }

    @Override
    public boolean needsBindings() {
        for (Expression child : children) {
            if (child.needsBindings())
                return true;
        }
        return false;
    }

    @Override
    public AkType valueType() {
        return type;
    }

    // Object interface

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        buildToString(sb);
        return sb.toString();
    }

    // for use by subclasses

    protected abstract void describe(StringBuilder sb);

    protected final List<? extends Expression> children() {
        return children;
    }

    protected AbstractCompositeExpression(AkType type, List<? extends Expression> children) {
        this.children = children.isEmpty()
                ? Collections.<Expression>emptyList()
                : Collections.unmodifiableList(new ArrayList<Expression>(children));
        this.type = type;
    }

    protected List<? extends ExpressionEvaluation> childrenEvaluations() {
        List<ExpressionEvaluation> result = new ArrayList<ExpressionEvaluation>();
        for (Expression expression : children) {
            result.add(expression.evaluation());
        }
        return result;
    }

    // for use in this class

    /**
     * Builds a description of this instance into the specified StringBuilder. The general format is:
     * <tt>ClassName(description -&gt; arg0, arg1, ...)</tt>.
     * @param sb the output
     */
    private void buildToString(StringBuilder sb) {
        String className = getClass().getSimpleName();
        sb.append(className);
        // chop off "-Expression" if it's there
        if (className.endsWith(EXPRESSION_SUFFIX)) {
            sb.setLength(sb.length() - EXPRESSION_SUFFIX.length());
        }
        sb.append('(');
        describe(sb);
        sb.append(" -> ");
        for (Iterator<? extends Expression> iterator = children.iterator(); iterator.hasNext(); ) {
            Expression child = iterator.next();
            if (child instanceof AbstractCompositeExpression) {
                AbstractCompositeExpression ace = (AbstractCompositeExpression) child;
                ace.buildToString(sb);
            } else {
                sb.append(child);
            }
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append(" -> ").append(valueType()).append(')');
    }

    // object state

    private final List<? extends Expression> children;
    private final AkType type;

    // const

    private static final String EXPRESSION_SUFFIX = "Expression";
}
