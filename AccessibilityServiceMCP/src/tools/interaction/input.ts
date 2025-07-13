/**
 * Input Interaction Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerInputTools(server: McpServer) {
  server.tool(
    "input_text",
    "Nhập text vào field đang focus",
    {
      text: z.string().describe("Text cần nhập"),
      clearFirst: z.boolean().optional().default(false).describe("Xóa text hiện tại trước khi nhập")
    },
    async ({ text, clearFirst }) => {
      try {
        await apiClient.inputText({ text, clearFirst });
        return {
          content: [
            {
              type: "text",
              text: `✅ **Nhập text thành công:** "${text}"${clearFirst ? " (đã xóa text cũ)" : ""}`
            }
          ]
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi nhập text:** ${error instanceof Error ? error.message : String(error)}`
            }
          ]
        };
      }
    }
  );
}
