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
        const health = await apiClient.health();
        return {
          content: [
            {
              type: "text",
              text:
                `✅ **API Server đang hoạt động**\n\n` +
                `📊 **Thông tin:**\n` +
                `- Trạng thái: ${health.status}\n` +
                `- Uptime: ${Math.floor(health.uptime / 1000)} giây\n` +
                `- Timestamp: ${new Date(health.timestamp).toLocaleString()}`,
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi kết nối API**\n\n${
                error instanceof Error ? error.message : String(error)
              }`,
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
        const deviceInfo = await apiClient.getDeviceInfo();
        return {
          content: [
            {
              type: "text",
              text:
                `📱 **Thông tin thiết bị**\n\n` +
                `- Model: ${deviceInfo.deviceModel}\n` +
                `- Android: ${deviceInfo.androidVersion}\n` +
                `- Màn hình: ${deviceInfo.screenWidth}x${deviceInfo.screenHeight}\n` +
                `- Density: ${deviceInfo.density}\n` +
                `- Orientation: ${deviceInfo.orientation}`,
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi:** ${
                error instanceof Error ? error.message : String(error)
              }`,
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
        const base64Image = await apiClient.getScreenshot();
        return {
          content: [
            {
              type: "text",
              text: "✅ **Screenshot đã được chụp**",
            },
            {
              type: "image",
              data: base64Image,
              mimeType: "image/png",
            },
          ],
        };
      } catch (error) {
        return {
          content: [
            {
              type: "text",
              text: `❌ **Lỗi chụp ảnh:** ${
                error instanceof Error ? error.message : String(error)
              }`,
            },
          ],
        };
      }
    }
  );
}
