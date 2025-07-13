/**
 * Basic Navigation Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { apiClient } from "../../api/client.js";

export function registerNavigationTools(server: McpServer) {
  server.tool(
    "navigate_home",
    "Về màn hình chính",
    {},
    async () => {
      try {
        await apiClient.home();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Đã về màn hình chính**"
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
    "navigate_back",
    "Quay lại màn hình trước",
    {},
    async () => {
      try {
        await apiClient.back();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Đã quay lại**"
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
    "open_recent_apps",
    "Mở danh sách ứng dụng gần đây",
    {},
    async () => {
      try {
        await apiClient.recent();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Đã mở danh sách ứng dụng gần đây**"
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
