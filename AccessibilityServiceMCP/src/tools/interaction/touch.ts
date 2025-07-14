/**
 * Touch Interaction Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerTouchTools(server: McpServer) {
  server.tool(
    "click",
    "Click tại tọa độ cụ thể trên màn hình - SỬ DỤNG SAU find_elements để lấy tọa độ chính xác",
    {
      x: z.number().describe("Tọa độ X"),
      y: z.number().describe("Tọa độ Y"),
    },
    async ({ x, y }) => {
      try {
        const result = await apiClient.click({ x, y });
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
    "long_click",
    "Long click (nhấn giữ) tại tọa độ cụ thể - SỬ DỤNG SAU find_elements để lấy tọa độ chính xác",
    {
      x: z.number().describe("Tọa độ X"),
      y: z.number().describe("Tọa độ Y"),
    },
    async ({ x, y }) => {
      try {
        const result = await apiClient.longClick({ x, y });
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
    "double_click",
    "Double click tại tọa độ cụ thể - SỬ DỤNG SAU find_elements để lấy tọa độ chính xác",
    {
      x: z.number().describe("Tọa độ X"),
      y: z.number().describe("Tọa độ Y"),
    },
    async ({ x, y }) => {
      try {
        const result = await apiClient.doubleClick({ x, y });
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
    "swipe",
    "Vuốt từ điểm này đến điểm khác - SỬ DỤNG SAU find_elements để lấy tọa độ start/end chính xác",
    {
      startX: z.number().describe("Tọa độ X bắt đầu"),
      startY: z.number().describe("Tọa độ Y bắt đầu"),
      endX: z.number().describe("Tọa độ X kết thúc"),
      endY: z.number().describe("Tọa độ Y kết thúc"),
      duration: z.number().optional().describe("Thời gian vuốt (ms)"),
    },
    async ({ startX, startY, endX, endY, duration }) => {
      try {
        const result = await apiClient.swipe({
          startX,
          startY,
          endX,
          endY,
          duration,
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
    "scroll",
    "Cuộn màn hình theo hướng chỉ định - Có thể dùng độc lập hoặc sau find_elements với actionType='scroll' để tìm scrollable areas",
    {
      direction: z.enum(["UP", "DOWN", "LEFT", "RIGHT"]).describe("Hướng cuộn"),
      distance: z.number().optional().describe("Khoảng cách cuộn (pixels)"),
    },
    async ({ direction, distance }) => {
      try {
        const result = await apiClient.scroll({ direction, distance });
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
