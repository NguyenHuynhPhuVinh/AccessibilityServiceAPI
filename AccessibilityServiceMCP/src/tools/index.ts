/**
 * Tools Registry - Đăng ký tất cả MCP tools
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";

// Import các tool modules
import { registerHealthTools } from "./system/health.js";
import { registerSystemControlTools } from "./system/controls.js";
import { registerUiElementsTools } from "./ui/elements.js";
import { registerTouchTools } from "./interaction/touch.js";
import { registerInputTools } from "./interaction/input.js";
import { registerNavigationTools } from "./navigation/basic.js";
import { registerAppManagementTools } from "./app/management.js";

/**
 * Đăng ký tất cả tools cho Android Accessibility Service
 */
export function registerAllTools(server: McpServer) {
  // System tools
  registerHealthTools(server);
  registerSystemControlTools(server);
  
  // UI tools
  registerUiElementsTools(server);
  
  // Interaction tools
  registerTouchTools(server);
  registerInputTools(server);
  
  // Navigation tools
  registerNavigationTools(server);
  
  // App management tools
  registerAppManagementTools(server);

  console.error("✅ Đã đăng ký tất cả Android Accessibility Service tools");
}
