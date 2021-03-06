/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.facelets.tag;

import javax.faces.view.facelets.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * @author Jacob Hookom
 * @version $Id$
 */
final class BeanPropertyTagRule extends MetaRule {
    
    final static class LiteralPropertyMetadata extends Metadata {
        
        private final Method method;

        private final TagAttribute attribute;

        private Object[] value;

        public LiteralPropertyMetadata(Method method, TagAttribute attribute) {
            this.method = method;
            this.attribute = attribute;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            if (value == null) {
                String str = this.attribute.getValue();
                value = new Object[] { ctx.getExpressionFactory().coerceToType(str,
                        method.getParameterTypes()[0]) };
            }
            try {
                method.invoke(instance, this.value);
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(this.attribute, e.getCause());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TagAttributeException(this.attribute, e);
            }
        }

    }
    
    final static class DynamicPropertyMetadata extends Metadata {

        private final Method method;

        private final TagAttribute attribute;

        private final Class type;

        public DynamicPropertyMetadata(Method method, TagAttribute attribute) {
            this.method = method;
            this.type = method.getParameterTypes()[0];
            this.attribute = attribute;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            try {
                this.method.invoke(instance, new Object[] { this.attribute
                        .getObject(ctx, this.type) });
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(this.attribute, e.getCause());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TagAttributeException(this.attribute, e);
            }
        }
    }
    
    public final static BeanPropertyTagRule Instance = new BeanPropertyTagRule();

    @Override
    public Metadata applyRule(String name, TagAttribute attribute,
            MetadataTarget meta) {
        Method m = meta.getWriteMethod(name);

        // if the property is writable
        if (m != null) {
            if (attribute.isLiteral()) {
                return new LiteralPropertyMetadata(m, attribute);
            } else {
                return new DynamicPropertyMetadata(m, attribute);
            }
        }

        return null;
    }

}
