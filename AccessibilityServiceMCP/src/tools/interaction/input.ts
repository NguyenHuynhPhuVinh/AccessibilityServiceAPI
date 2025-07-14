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
      clearFirst: z
        .boolean()
        .optional()
        .default(false)
        .describe("Xóa text hiện tại trước khi nhập"),
    },
    async ({ text, clearFirst }) => {
      try {
        const result = await apiClient.inputText({ text, clearFirst });
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
