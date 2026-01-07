package cloud.timo.TimoCloud.api.utils;

import cloud.timo.TimoCloud.core.objects.Proxy;
import cloud.timo.TimoCloud.core.objects.Server;
import cloud.timo.TimoCloud.core.utils.paperapi.PaperAPI;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class PaperAPITest {

    // This test checks if the PaperAPI is working correctly
    @Test
    public void paperAPITest() {
        for (PaperAPI.Project value : PaperAPI.Project.values()) {
            List<String> versions = PaperAPI.getVersions(value);
            for (String version : versions) {
                JsonObject latestBuilds = PaperAPI.getLatestBuilds(value, version);
                String downloadURL = PaperAPI.buildDownloadURL(latestBuilds);
                String fileName = PaperAPI.getFileName(latestBuilds);
                boolean downloadLink = isDownloadLink(downloadURL);
                assertTrue("Download of project " + fileName + " failed", downloadLink);
            }
        }
        assertTrue("No Paper-Projects support Proxy", Arrays.stream(PaperAPI.Project.values()).anyMatch(project -> project.isSupported(Proxy.class)));
        assertTrue("No Server-Projects support Proxy", Arrays.stream(PaperAPI.Project.values()).anyMatch(project -> project.isSupported(Server.class)));


    }

  private boolean isDownloadLink(String urlString) {
    try {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        conn.setRequestProperty("Accept", "*/*");

        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setInstanceFollowRedirects(true);

        int status = conn.getResponseCode();

        if (status != HttpURLConnection.HTTP_OK) {
            return false;
        }

        String contentType = conn.getContentType();
        String contentDisposition = conn.getHeaderField("Content-Disposition");

        return (contentType != null && (
                contentType.contains("application/octet-stream") ||
                contentType.contains("application/java-archive") ||
                contentType.contains("zip") ||
                contentType.contains("jar"))
        ) || (contentDisposition != null && contentDisposition.contains("attachment"));

    } catch (Exception e) {
        return false;
    }
}
}
