/* (c) 2014 - 2016 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.rest.catalog;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geoserver.data.test.SystemTestData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.geoserver.rest.RestBaseController.ROOT_PATH;

public class TemplateControllerTest extends CatalogRESTTestSupport {

    public void setUpTestData(SystemTestData testData) throws Exception {
        super.setUpTestData(testData);

        testData.setUpDefaultRasterLayers();
    }

    @Before
    public void login() throws Exception {
        login("admin", "geoserver", "ROLE_ADMINISTRATOR");
    }

    private String getIndexAsString(String childPath, String format) throws Exception {
        String indexUrl = childPath.substring(0, childPath.lastIndexOf("/"));
        if (format != null) {
            indexUrl += "." + format;
        }
        String indexContent = getAsString(indexUrl);
        return indexContent;
    }

    private static String getName(String path) {
        return path.substring(path.lastIndexOf('/') + 1, path.length());
    }

    private void testGetPutGetDeleteGet(String path, String content) throws Exception {
        String name = getName(path);

        String htmlIndexToken = "geoserver" + path + "\">" + name + "</a></li>";
        String xmlIndexToken = "<name>" + name + "</name>";
        String jsonIndexToken = "{\"name\":\"" + name + "\"";

        // GET
        assertEquals("File Not Found", getAsString(path).trim());
        assertFalse(getIndexAsString(path, null).contains(htmlIndexToken));
        assertFalse(getIndexAsString(path, "html").contains(htmlIndexToken));
        assertFalse(getIndexAsString(path, "xml").contains(xmlIndexToken));
        assertFalse(getIndexAsString(path, "json").contains(jsonIndexToken));

        // PUT
        put(path, content).close();
        assertTrue(getIndexAsString(path, null).contains(htmlIndexToken));
        assertTrue(getIndexAsString(path, "html").contains(htmlIndexToken));
        assertTrue(getIndexAsString(path, "xml").contains(xmlIndexToken));
        assertTrue(getIndexAsString(path, "json").contains(jsonIndexToken));

        // GET
        assertEquals(content, getAsString(path).trim());

        // DELETE
        assertEquals(200, deleteAsServletResponse(path).getStatus());

        // GET
        assertEquals("File Not Found", getAsString(path).trim());
        assertFalse(getIndexAsString(path, null).contains(htmlIndexToken));
        assertFalse(getIndexAsString(path, "html").contains(htmlIndexToken));
        assertFalse(getIndexAsString(path, "xml").contains(xmlIndexToken));
        assertFalse(getIndexAsString(path, "json").contains(jsonIndexToken));
    }

    @Test
    public void testGetPutGetDeleteGet() throws Exception {
        String path = ROOT_PATH+"/templates/my_template.ftl";
        testGetPutGetDeleteGet(path, "hello world");
    }

    private List<String> getAllPaths() {
        List<String> paths = new ArrayList<String>();

        paths.add(ROOT_PATH+"/templates/aTemplate.ftl");
        paths.add(ROOT_PATH+"/templates/anotherTemplate.ftl");

        paths.add(ROOT_PATH+"/workspaces/topp/templates/aTemplate.ftl");
        paths.add(ROOT_PATH+"/workspaces/topp/templates/anotherTemplate.ftl");

        paths.add(ROOT_PATH+"/workspaces/topp/datastores/states_shapefile/templates/aTemplate.ftl");
        paths.add(
                ROOT_PATH+"/workspaces/topp/datastores/states_shapefile/templates/anotherTemplate.ftl");

        paths.add(
                ROOT_PATH+"/workspaces/topp/datastores/states_shapefile/featuretypes/states/templates/aTemplate.ftl");
        paths.add(
                ROOT_PATH+"/workspaces/topp/datastores/states_shapefile/featuretypes/states/templates/anotherTemplate.ftl");

        paths.add(ROOT_PATH+"/workspaces/wcs/coveragestores/DEM/templates/aTemplate.ftl");
        paths.add(ROOT_PATH+"/workspaces/wcs/coveragestores/DEM/templates/anotherTemplate.ftl");

        paths.add(
                ROOT_PATH+"/workspaces/wcs/coveragestores/DEM/coverages/tazdem.tiff/templates/aTemplate.ftl");
        paths.add(
                ROOT_PATH+"/workspaces/wcs/coveragestores/DEM/coverages/tazdem.tiff/templates/anotherTemplate.ftl");

        return paths;
    }

    @Test
    public void testAllPathsSequentially() throws Exception {
        Random random = new Random();
        for (String path : getAllPaths()) {
            testGetPutGetDeleteGet(path, "hello test " + random.nextInt(1000));
        }
    }

    @Test
    public void testAllPaths() throws Exception {
        String contentHeader = "hello path ";
        List<String> paths = getAllPaths();

        for (String path : paths) {
            // GET
            MockHttpServletResponse response = getAsServletResponse(path);
            assertEquals( 404, response.getStatus() );
            String string = response.getContentAsString();
            if( !string.contains("not found")){
                fail("Expected 'not found' for '"+ path+"':'"+string+"'");
            }
        }

        for (String path : paths) {
            // PUT
            put(path, contentHeader + path).close();
        }

        for (String path : paths) {
            // GET
            assertEquals(contentHeader + path, getAsString(path).trim());
        }

        for (String path : paths) {
            // DELETE
            assertEquals(200, deleteAsServletResponse(path).getStatus());
        }

        for (String path : paths) {
            // GET
            assertEquals("File Not Found", getAsString(path).trim());
        }
    }

    @Test
    public void testUpdate() throws Exception {
        String fooTemplate = ROOT_PATH+"/templates/foo.ftl";
        String barTemplate = ROOT_PATH+"/templates/bar.ftl";

        String fooContent = "hello foo - longer than bar";
        String barContent = "hello bar";

        // PUT
        put(fooTemplate, fooContent).close();
        put(barTemplate, barContent).close();

        // GET
        assertEquals(fooContent, getAsString(fooTemplate).trim());
        assertEquals(barContent, getAsString(barTemplate).trim());

        fooContent = "goodbye foo";

        // PUT
        put(fooTemplate, fooContent).close();

        // GET
        assertEquals(fooContent, getAsString(fooTemplate).trim());
        assertEquals(barContent, getAsString(barTemplate).trim());
    }
}
