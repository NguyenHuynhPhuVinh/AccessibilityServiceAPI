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
      stream: z.enum(["MUSIC", "RING", "NOTIFICATION", "ALARM"]).optional()
        .describe("Loại âm thanh cần điều chỉnh")
    },
    async ({ direction, stream }) => {
      try {
        await apiClient.setVolume({ direction, stream });
        return {
          content: [
            {
              type: "text",
              text: `✅ **Điều chỉnh âm lượng thành công:** ${direction} ${stream ? `(${stream})` : ""}`
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi điều chỉnh âm lượng:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );

  server.tool(
    "open_notifications",
    "Mở panel thông báo",
    {},
    async () => {
      try {
        await apiClient.openNotifications();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Đã mở panel thông báo**"
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

  server.tool(
    "open_quick_settings",
    "Mở quick settings panel",
    {},
    async () => {
      try {
        await apiClient.openQuickSettings();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Đã mở quick settings**"
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
