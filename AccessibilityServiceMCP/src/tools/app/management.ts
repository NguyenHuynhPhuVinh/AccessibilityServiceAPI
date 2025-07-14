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
      packageName: z.string().optional().describe("Package name của app"),
    },
    async ({ appName, packageName }) => {
      try {
        const result = await apiClient.clickApp({ appName, packageName });
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(result, null, 2),
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: error instanceof Error ? error.message : String(error),
                },
                null,
                2
              ),
            },
          ],
        };
      }
    }
  );

  server.tool(
    "launch_app",
    "Mở app bằng package name",
    {
      packageName: z.string().describe("Package name của app cần mở"),
    },
    async ({ packageName }) => {
      try {
        const result = await apiClient.launchApp({ packageName });
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(result, null, 2),
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: error instanceof Error ? error.message : String(error),
                },
                null,
                2
              ),
            },
          ],
        };
      }
    }
  );

  server.tool(
    "close_app",
    "Đóng app hiện tại",
    {
      packageName: z
        .string()
        .optional()
        .describe("Package name của app cần đóng"),
    },
    async ({ packageName }) => {
      try {
        const result = await apiClient.closeApp({ packageName });
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(result, null, 2),
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: error instanceof Error ? error.message : String(error),
                },
                null,
                2
              ),
            },
          ],
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
        const result = await apiClient.getRecentApps();

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(result, null, 2),
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                {
                  error: error instanceof Error ? error.message : String(error),
                },
                null,
                2
              ),
            },
          ],
        };
      }
    }
  );
}
