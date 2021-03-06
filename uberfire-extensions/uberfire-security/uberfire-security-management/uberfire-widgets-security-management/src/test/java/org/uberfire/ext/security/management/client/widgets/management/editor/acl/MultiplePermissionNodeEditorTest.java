/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.ext.security.management.client.widgets.management.editor.acl;

import java.util.Arrays;
import javax.enterprise.event.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.uberfire.ext.security.management.client.widgets.management.editor.acl.node.MultiplePermissionNodeEditor;
import org.uberfire.ext.security.management.client.widgets.management.editor.acl.node.PermissionNodeEditor;
import org.uberfire.ext.security.management.client.widgets.management.editor.acl.node.PermissionSwitch;
import org.uberfire.ext.security.management.client.widgets.management.editor.acl.node.PermissionWidgetFactory;
import org.uberfire.ext.security.management.client.widgets.management.events.PermissionChangedEvent;
import org.uberfire.ext.security.management.client.widgets.management.events.PermissionNodeAddedEvent;
import org.uberfire.ext.security.management.client.widgets.management.events.PermissionNodeRemovedEvent;
import org.uberfire.ext.widgets.common.client.dropdown.LiveSearchDropDown;
import org.uberfire.mvp.Command;
import org.uberfire.security.authz.AuthorizationResult;
import org.uberfire.security.authz.Permission;
import org.uberfire.security.client.authz.tree.LoadCallback;
import org.uberfire.security.client.authz.tree.PermissionNode;
import org.uberfire.security.client.authz.tree.impl.PermissionGroupNode;
import org.uberfire.security.client.authz.tree.impl.PermissionResourceNode;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MultiplePermissionNodeEditorTest {

    @Mock
    MultiplePermissionNodeEditor.View view;

    @Mock
    PermissionSwitch.View permissionSwitchView;

    @Mock
    LiveSearchDropDown liveSearchDropDown;

    @Mock
    PermissionWidgetFactory widgetFactory;

    @Mock
    Event<PermissionChangedEvent> changedEvent;

    @Mock
    Event<PermissionNodeAddedEvent> nodeAddedEvent;

    @Mock
    Event<PermissionNodeRemovedEvent> nodeRemovedEvent;

    @Mock
    Permission permission1;

    @Mock
    Permission permission2;

    @Mock
    PermissionGroupNode permissionGroupNode;

    @Mock
    PermissionResourceNode permissionResourceNode;

    @Mock
    PermissionNode childNode1;

    @Mock
    PermissionNode childNode2;

    @Mock
    PermissionNodeEditor childEditor1;

    @Mock
    PermissionNodeEditor childEditor2;

    @Mock
    Command onChange;

    MultiplePermissionNodeEditor presenter;
    PermissionSwitch permissionSwitch1;
    PermissionSwitch permissionSwitch2;

    @Before
    public void setUp() {
        presenter = new MultiplePermissionNodeEditor(view, liveSearchDropDown, widgetFactory,
                changedEvent, nodeAddedEvent, nodeRemovedEvent);
        permissionSwitch1 = spy(new PermissionSwitch(permissionSwitchView));
        permissionSwitch2 = spy(new PermissionSwitch(permissionSwitchView));

        when(widgetFactory.createSwitch()).thenReturn(permissionSwitch1, permissionSwitch2);
        when(widgetFactory.createEditor(childNode1)).thenReturn(childEditor1);
        when(widgetFactory.createEditor(childNode2)).thenReturn(childEditor2);

        when(permission1.getResult()).thenReturn(AuthorizationResult.ACCESS_DENIED);
        when(permission1.getName()).thenReturn("p1");
        when(permission2.getResult()).thenReturn(AuthorizationResult.ACCESS_GRANTED);
        when(permission2.getName()).thenReturn("p2");

        when(permissionGroupNode.getNodeName()).thenReturn("r1");
        when(permissionGroupNode.getPermissionList()).thenReturn(Arrays.asList(permission1, permission2));
        when(permissionGroupNode.getPermissionGrantName(permission1)).thenReturn("grant1");
        when(permissionGroupNode.getPermissionDenyName(permission1)).thenReturn("deny1");
        when(permissionGroupNode.getPermissionGrantName(permission2)).thenReturn("grant2");
        when(permissionGroupNode.getPermissionDenyName(permission2)).thenReturn("deny2");

        when(permissionResourceNode.getNodeName()).thenReturn("r2");
        when(permissionResourceNode.getPermissionList()).thenReturn(Arrays.asList(permission1, permission2));
        when(permissionResourceNode.getPermissionGrantName(permission1)).thenReturn("grant1");
        when(permissionResourceNode.getPermissionDenyName(permission1)).thenReturn("deny1");
        when(permissionResourceNode.getPermissionGrantName(permission2)).thenReturn("grant2");
        when(permissionResourceNode.getPermissionDenyName(permission2)).thenReturn("deny2");

        doAnswer(invocationOnMock -> {
            LoadCallback callback = (LoadCallback) invocationOnMock.getArguments()[0];
            callback.afterLoad(Arrays.asList(childNode1, childNode2));
            return null;
        }).when(permissionGroupNode).expand(any(LoadCallback.class));

        doAnswer(invocationOnMock -> {
            LoadCallback callback = (LoadCallback) invocationOnMock.getArguments()[0];
            callback.afterLoad(Arrays.asList(childNode1, childNode2));
            return null;
        }).when(permissionResourceNode).expand(any(LoadCallback.class));
    }
    
    @Test
    public void testInitGroupNode() {
        presenter.edit(permissionGroupNode);

        assertEquals(presenter.getChildEditors().size(), 2);
        assertEquals(presenter.getPermissionNode(), permissionGroupNode);

        verify(view).setNodeName("r1");
        verify(view, never()).setNodeFullName(anyString());
        verify(view).addPermission(permissionSwitch1);
        verify(view).addPermission(permissionSwitch2);
        verify(permissionSwitch1).init(eq("grant1"), eq("deny1"), eq(false), eq(0));
        verify(permissionSwitch2).init(eq("grant2"), eq("deny2"), eq(true), eq(0));

        verify(view).setClearChildrenEnabled(false);
        verify(view, never()).setAddChildEnabled(true);
        verify(view, never()).setChildSelector(any());
        verifyZeroInteractions(liveSearchDropDown);
    }

    @Test
    public void testInitResourceNode() {
        presenter.edit(permissionResourceNode);

        assertEquals(presenter.getChildEditors().size(), 2);
        assertEquals(presenter.getPermissionNode(), permissionResourceNode);

        verify(view).setAddChildEnabled(true);
        verify(view).setChildSelector(liveSearchDropDown);
    }


    @Test
    public void testExpandGroupNode() {
        presenter.edit(permissionGroupNode);
        presenter.onNodeClick();

        verify(view).addChildEditor(childEditor1, false);
        verify(view).addChildEditor(childEditor2, false);
        verify(view).setAddChildEnabled(false);
        verify(view, never()).setAddChildEnabled(true);
        verify(view, never()).setChildSelector(any());
        verify(view, never()).setClearChildrenEnabled(true);
        verify(childEditor1).edit(childNode1);
        verify(childEditor2).edit(childNode2);
    }

    @Test
    public void testExpandResourceNode() {
        presenter.edit(permissionResourceNode);
        presenter.onNodeClick();

        verify(view).addChildEditor(childEditor1, true);
        verify(view).addChildEditor(childEditor2, true);
        verify(view).setAddChildEnabled(true);
        verify(view).setChildSelector(any());
        verify(view, atLeastOnce()).setClearChildrenEnabled(true);
        verify(childEditor1).edit(childNode1);
        verify(childEditor2).edit(childNode2);
    }

    @Test
    public void testSwitchChange() {
        presenter.edit(permissionGroupNode);

        permissionSwitch1.onChange();

        verify(permission1).setResult(any());
        verify(changedEvent).fire(any());
    }
}
