/**
 * UI Elements Tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiClient } from "../../api/client.js";

export function registerUiElementsTools(server: McpServer) {
  server.tool(
    "find_elements",
    "Tìm kiếm UI elements trên màn hình với smart fallback",
    {
      text: z.string().optional().describe("Text cần tìm"),
      contentDescription: z.string().optional().describe("Content description cần tìm"),
      className: z.string().optional().describe("Class name cần tìm"),
      actionType: z.enum(["click", "input", "scroll", "check", "read"]).optional()
        .describe("Loại action muốn thực hiện để quyết định fallback type")
    },
    async ({ text, contentDescription, className, actionType }) => {
      try {
        const result = await apiClient.findElements({
          text,
          contentDescription,
          className,
          actionType
        });

        let responseText = "";
        
        if (result.count > 0) {
          responseText = `✅ **Tìm thấy ${result.count} elements**\n\n`;
          result.elements.forEach((element, index) => {
            responseText += `**Element ${index + 1}:**\n`;
            responseText += `- Text: ${element.text || "N/A"}\n`;
            responseText += `- Description: ${element.contentDescription || "N/A"}\n`;
            responseText += `- Class: ${element.className || "N/A"}\n`;
            responseText += `- Bounds: (${element.bounds.left},${element.bounds.top}) to (${element.bounds.right},${element.bounds.bottom})\n`;
            responseText += `- Center: (${element.bounds.centerX},${element.bounds.centerY})\n`;
            responseText += `- Clickable: ${element.isClickable}\n`;
            responseText += `- Editable: ${element.isEditable}\n\n`;
          });
        } else if (result.fallback) {
          responseText = `🔄 **Fallback Mode** (${result.fallbackType})\n\n`;
          responseText += `${result.message}\n\n`;
          
          if (result.clickableElements && result.clickableElements.length > 0) {
            responseText += `🖱️ **Clickable Elements (${result.totalClickable}):**\n`;
            result.clickableElements.slice(0, 5).forEach((element, index) => {
              responseText += `${index + 1}. ${element.contentDescription || element.text || element.className} `;
              responseText += `at (${element.bounds.centerX},${element.bounds.centerY})\n`;
            });
            if (result.clickableElements.length > 5) {
              responseText += `... và ${result.clickableElements.length - 5} elements khác\n`;
            }
            responseText += "\n";
          }
          
          if (result.editableElements && result.editableElements.length > 0) {
            responseText += `✏️ **Editable Elements (${result.totalEditable}):**\n`;
            result.editableElements.forEach((element, index) => {
              responseText += `${index + 1}. ${element.contentDescription || element.text || element.className} `;
              responseText += `at (${element.bounds.centerX},${element.bounds.centerY})\n`;
            });
            responseText += "\n";
          }
          
          if (result.scrollableElements && result.scrollableElements.length > 0) {
            responseText += `📜 **Scrollable Elements (${result.totalScrollable}):**\n`;
            result.scrollableElements.forEach((element, index) => {
              responseText += `${index + 1}. ${element.className} `;
              responseText += `at (${element.bounds.centerX},${element.bounds.centerY})\n`;
            });
            responseText += "\n";
          }
        } else {
          responseText = "❌ **Không tìm thấy elements nào**";
        }

        return {
          content: [
            {
              type: "text",
              text: responseText
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
    "get_ui_tree",
    "Lấy cây UI đầy đủ của màn hình hiện tại",
    {},
    async () => {
      try {
        const uiTree = await apiClient.getUiTree();
        return {
          content: [
            {
              type: "text",
              text: `📱 **UI Tree**\n\n` +
                   `- Total nodes: ${uiTree.totalNodes}\n` +
                   `- Capture time: ${new Date(uiTree.captureTime).toLocaleString()}\n\n` +
                   `**Root Node:**\n` +
                   `${JSON.stringify(uiTree.rootNode, null, 2)}`
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
