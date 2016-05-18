/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.integration.test.css;

import java.util.HashMap;
import java.util.Map;

import org.auraframework.Aura;
import org.auraframework.def.ActionDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.StyleDef;
import org.auraframework.def.TokensDef;
import org.auraframework.impl.controller.DynamicStylingController;
import org.auraframework.impl.css.StyleTestCase;
import org.auraframework.instance.Action;
import org.auraframework.instance.Action.State;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Basic tests for {@link DynamicStylingController}.
 * <p>
 * More tests in test/components/dynamicStylingTest
 */
public class DynamicStylingControllerTest extends StyleTestCase {
    private static final String ACTION = "java://org.auraframework.impl.controller.DynamicStylingController/ACTION$applyTokens";

    /** test basic usage */
    @Test
    public void testApplyTokens() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(".THIS{margin: 10px; color: t(color);}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    /** at-rules with tokens inside should be included if applicable */
    @Test
    public void testTokenInsideMediaQuery() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@media screen {.THIS{margin: 10px; color: t(color);}} .THIS {color: red}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format("@media screen {\n  .%s {color:green}\n}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    /** at rules without tokens inside should not be included */
    @Test
    public void testNoTokenInsideMediaQuery() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@media screen {.THIS{margin: 10px;}} .THIS {color:t(color)}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    /** at-rules using a token directly should be included if applicable */
    @Test
    public void testMediaQueryUsingTokenDirectly() throws Exception {
        addNsTokens(tokens().token("query", "all and (min-width:300px)"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("query", "screen"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@media t(query) {.THIS{color:red}}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format("@media screen {\n  .%s {color:red}\n}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    /** at-rules not using a token anywhere should not be included */
    @Test
    public void testMediaQueryNotUsingTokenDirectlyNorInside() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@media screen {.THIS{color:red}} .THIS{color: t(color);}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    @Test
    public void testTokenInsideConditional() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@if(OTHER) {.THIS{margin: 10px; color: t(color);}} .THIS {color: red}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    @Test
    public void testNoTokenInsideConditional() throws Exception {
        addNsTokens(tokens().token("color", "red"));
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color", "green"));

        DefDescriptor<StyleDef> style = addContextAppBundleStyle(
                "@if(OTHER) {.THIS{margin: 10px}} .THIS {color: t(color)}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    /** if a var is an alias, references to the aliased var should be included */
    @Test
    public void testCrossReference() throws Exception {
        // color2 points to color1
        addNsTokens(tokens().token("color1", "red").token("color2", "{!color1}").token("color3", "yellow"));

        // token overrides color1
        DefDescriptor<TokensDef> toApply = addSeparateTokens(tokens().token("color1", "green"));

        // style points to color2, it should be included because color1 is overridden
        DefDescriptor<StyleDef> style = addContextAppBundleStyle(".THIS{color: t(color2); background: t(color3);}");
        addContextApp("<aura:application/>");

        Action action = runAction(toApply.getDescriptorName());
        assertEquals("errors:" + action.getErrors(), State.SUCCESS, action.getState());

        String expected = String.format(".%s {color:green}\n", 
        		definitionService.getDefinition(style).getClassName());
        assertEquals(expected, action.getReturnValue());
    }

    private Action runAction(String descriptor) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("descriptors", Lists.newArrayList(descriptor));
        params.put("extraStyles", ImmutableList.<String>of());
        Action action = (Action) Aura.getInstanceService().getInstance(ACTION, ActionDef.class, params);
        action.run();
        return action;
    }
}
