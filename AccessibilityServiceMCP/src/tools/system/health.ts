/**
 * System Health Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { apiClient } from "../../api/client.js";

export function registerHealthTools(server: McpServer) {
  server.tool(
    "health_check",
    "Kiểm tra trạng thái API server trên thiết bị Android - SỬ DỤNG ĐẦU TIÊN để đảm bảo kết nối",
    {},
    async () => {
      try {
        const result = await apiClient.health();
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
    "get_device_info",
    "Lấy thông tin thiết bị Android",
    {},
    async () => {
      try {
        const result = await apiClient.getDeviceInfo();
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
    "get_screenshot",
    "Chụp ảnh màn hình thiết bị - CHỈ dùng khi cần thiết, KHÔNG dùng để kiểm tra trạng thái UI (hãy dùng find_elements thay thế)",
    {},
    async () => {
      try {
        const result = await apiClient.getScreenshot();
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(
                { message: "Screenshot captured successfully" },
                null,
                2
              ),
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
