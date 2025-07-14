/**
 * Basic Navigation Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { apiClient } from "../../api/client.js";

export function registerNavigationTools(server: McpServer) {
  server.tool("navigate_home", "Về màn hình chính", {}, async () => {
    try {
      const result = await apiClient.home();
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
  });

  server.tool("navigate_back", "Quay lại màn hình trước", {}, async () => {
    try {
      const result = await apiClient.back();
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
  });

  server.tool(
    "open_recent_apps",
    "Mở danh sách ứng dụng gần đây",
    {},
    async () => {
      try {
        const result = await apiClient.recent();
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
