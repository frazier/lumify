package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.model.user.UserRowKey;
import com.altamiracorp.lumify.core.model.workspace.Workspace;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.altamiracorp.lumify.web.routes.RouteTestBase;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkspaceByRowKeyTest extends RouteTestBase {
    private WorkspaceByRowKey workspaceByRowKey;
    private WorkspaceRowKey workspaceRowKey;

    @Mock
    private WorkspaceRepository mockWorkspaceRespository;
    @Mock
    private User mockUser;
    @Mock
    private HttpSession mockHttpSession;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        workspaceByRowKey = new WorkspaceByRowKey(mockWorkspaceRespository);
        workspaceRowKey = new WorkspaceRowKey("lumify", "Default - lumify");
        when(mockRequest.getAttribute("workspaceRowKey")).thenReturn(workspaceRowKey.toString());
        when(mockRequest.getSession()).thenReturn(mockHttpSession);
        when(AuthenticationProvider.getUser(mockHttpSession)).thenReturn(mockUser);
    }

    @Test
    public void testHandle() throws Exception {
        Workspace workspace = new Workspace(workspaceRowKey);
        workspace.getMetadata().setCreator("lumify");
        when(mockWorkspaceRespository.findByRowKey(workspaceRowKey.toString(), mockUser.getModelUserContext())).thenReturn(workspace);
        when(mockRequest.getSession()).thenReturn(mockHttpSession);

        UserRowKey userRowKey = new UserRowKey("lumify");
        when(mockUser.getRowKey()).thenReturn(userRowKey.toString());

        workspaceByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);
        JSONObject responseJson = new JSONObject(responseStringWriter.getBuffer().toString());
        assertEquals(workspaceRowKey.toString(), responseJson.getString("id"));
        assertFalse(responseJson.getBoolean("isSharedToUser"));
        assertEquals("lumify", responseJson.getString("createdBy"));
        assertTrue(responseJson.getBoolean("isEditable"));
    }

    @Test
    public void testHandleWithNullWorkspace() throws Exception {
        when(mockWorkspaceRespository.findByRowKey(workspaceRowKey.toString(), mockUser.getModelUserContext())).thenReturn(null);
        workspaceByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testHandleWithNullResult() throws Exception {
        Workspace workspace = new Workspace(workspaceRowKey);
        workspace.getMetadata().setCreator("");
        when(mockWorkspaceRespository.findByRowKey(workspaceRowKey.toString(), mockUser.getModelUserContext())).thenReturn(workspace);
        when(workspace.toJson(mockUser)).thenReturn(null);

        workspaceByRowKey.handle(mockRequest, mockResponse, mockHandlerChain);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}