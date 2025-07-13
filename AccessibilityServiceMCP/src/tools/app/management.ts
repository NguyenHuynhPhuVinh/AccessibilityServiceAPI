/**
 * App Management Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerAppManagementTools(server: McpServer) {
  server.tool(
    "click_app",
    "Click vào app trên home screen hoặc app drawer",
    {
      appName: z.string().optional().describe("Tên app cần click"),
      packageName: z.string().optional().describe("Package name của app")
    },
    async ({ appName, packageName }) => {
      try {
        await apiClient.clickApp({ appName, packageName });
        return {
          content: [
            {
              type: "text",
              text: `✅ **Click app thành công:** ${appName || packageName}`
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi click app:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );

  server.tool(
    "launch_app",
    "Mở app bằng package name",
    {
      packageName: z.string().describe("Package name của app cần mở")
    },
    async ({ packageName }) => {
      try {
        await apiClient.launchApp({ packageName });
        return {
          content: [
            {
              type: "text",
              text: `✅ **Mở app thành công:** ${packageName}`
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi mở app:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );

  server.tool(
    "close_app",
    "Đóng app hiện tại",
    {
      packageName: z.string().optional().describe("Package name của app cần đóng")
    },
    async ({ packageName }) => {
      try {
        await apiClient.closeApp({ packageName });
        return {
          content: [
            {
              type: "text",
              text: `✅ **Đóng app thành công:** ${packageName || "app hiện tại"}`
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi đóng app:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );

  server.tool(
    "get_recent_apps",
    "Lấy danh sách ứng dụng gần đây",
    {},
    async () => {
      try {
        const recentApps = await apiClient.getRecentApps();
        let responseText = `📱 **Ứng dụng gần đây (${recentApps.totalApps}):**\n\n`;
        
        recentApps.apps.forEach((app, index) => {
          responseText += `${index + 1}. **${app.appName}**\n`;
          responseText += `   - Package: ${app.packageName}\n`;
          responseText += `   - Position: ${app.position}\n\n`;
        });

        return {
          content: [
            {
              type: "text",
              text: responseText
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );
}
