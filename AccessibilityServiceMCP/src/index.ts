#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { APP_CONFIG } from "./config/config.js";

// Import tools registry
import { registerAllTools } from "./tools/index.js";

// Tạo server instance
const server = new McpServer({
  name: APP_CONFIG.NAME,
  version: APP_CONFIG.VERSION,
  capabilities: {
    resources: {},
    tools: {},
  },
});

// Đăng ký tất cả Android Accessibility Service tools
registerAllTools(server);

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error(
    `🤖 ${APP_CONFIG.NAME} v${APP_CONFIG.VERSION} đang chạy trên stdio\n` +
      `📱 Kết nối đến Android API tại: ${
        process.env.ACCESSIBILITY_API_HOST || "192.168.1.5"
      }:${process.env.ACCESSIBILITY_API_PORT || "8080"}\n` +
      `💡 Sử dụng file .env để tùy chỉnh IP address`
  );
}

main().catch((error) => {
  console.error("Lỗi nghiêm trọng trong main():", error);
  process.exit(1);
});
