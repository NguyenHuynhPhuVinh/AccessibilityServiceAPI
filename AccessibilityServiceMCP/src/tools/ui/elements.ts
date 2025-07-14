/**
 * UI Elements Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerUiElementsTools(server: McpServer) {
  server.tool(
    "find_elements",
    "🎯 TOOL CHÍNH - AI sẽ ưu tiên sử dụng tool này để tìm kiếm UI elements trước khi thực hiện bất kỳ action nào. Có smart fallback system trả về elements phù hợp khi không tìm thấy chính xác",
    {
      text: z.string().optional().describe("Text cần tìm"),
      contentDescription: z
        .string()
        .optional()
        .describe("Content description cần tìm"),
      className: z.string().optional().describe("Class name cần tìm"),
      actionType: z
        .enum(["click", "input", "scroll", "check", "read"])
        .optional()
        .describe("Loại action muốn thực hiện để quyết định fallback type"),
    },
    async ({ text, contentDescription, className, actionType }) => {
      try {
        const result = await apiClient.findElements({
          text,
          contentDescription,
          className,
          actionType,
        });
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
    "get_ui_tree",
    "Lấy cây UI đầy đủ của màn hình hiện tại - SỬ DỤNG KHI mới vào app/màn hình chưa biết đang ở đâu và có gì để làm. Sau đó dùng find_elements cho các tương tác cụ thể",
    {},
    async () => {
      try {
        const result = await apiClient.getUiTree();
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
