/**
 * System Control Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerSystemControlTools(server: McpServer) {
  server.tool(
    "set_volume",
    "Điều chỉnh âm lượng thiết bị",
    {
      direction: z.enum(["UP", "DOWN"]).describe("Tăng hoặc giảm âm lượng"),
      stream: z
        .enum(["MUSIC", "RING", "NOTIFICATION", "ALARM"])
        .optional()
        .describe("Loại âm thanh cần điều chỉnh"),
    },
    async ({ direction, stream }) => {
      try {
        const result = await apiClient.setVolume({ direction, stream });
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

  server.tool("open_notifications", "Mở panel thông báo", {}, async () => {
    try {
      const result = await apiClient.openNotifications();
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
    "open_quick_settings",
    "Mở quick settings panel",
    {},
    async () => {
      try {
        const result = await apiClient.openQuickSettings();
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
