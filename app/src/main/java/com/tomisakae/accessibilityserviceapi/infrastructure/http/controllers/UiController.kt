package com.tomisakae.accessibilityserviceapi.infrastructure.http.controllers

import com.tomisakae.accessibilityserviceapi.domain.models.FindElementsRequest
import com.tomisakae.accessibilityserviceapi.domain.models.WaitForElementRequest
import com.tomisakae.accessibilityserviceapi.infrastructure.accessibility.AccessibilityServiceManager
import fi.iki.elonen.NanoHTTPD

/**
 * Controller for UI-related endpoints
 */
class UiController(
    private val serviceManager: AccessibilityServiceManager
) : BaseController() {
    
    /**
     * GET /ui-tree
     */
    fun getUiTree(): NanoHTTPD.Response {
        val uiTreeData = serviceManager.getUiTreeManager().getCurrentUiTree()
        return createSuccessResponse(uiTreeData)
    }

    /**
     * GET /ui-tree-compact
     * Trả về UI tree gọn với thông tin cần thiết cho AI
     */
    fun getUiTreeCompact(): NanoHTTPD.Response {
        val uiTreeData = serviceManager.getUiTreeManager().getCurrentUiTree()
        val compactTree = createCompactTree(uiTreeData.rootNode)

        val compactResponse = mapOf(
            "summary" to mapOf(
                "totalNodes" to uiTreeData.totalNodes,
                "captureTime" to uiTreeData.captureTime,
                "clickableCount" to compactTree.clickableElements.size,
                "editableCount" to compactTree.editableElements.size,
                "scrollableCount" to compactTree.scrollableElements.size,
                "textCount" to compactTree.textElements.size
            ),
            "clickableElements" to compactTree.clickableElements,
            "editableElements" to compactTree.editableElements,
            "scrollableElements" to compactTree.scrollableElements,
            "textElements" to compactTree.textElements,
            "structure" to compactTree.structure
        )

        return createSuccessResponse(compactResponse)
    }


    
    /**
     * POST /find-elements
     * Tìm elements theo criteria, nếu không tìm thấy thì fallback về UI tree
     */
    fun findElements(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<FindElementsRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")

        val result = serviceManager.getUiTreeManager().findElements(request)

        // Nếu không tìm thấy elements nào, fallback về lite UI tree
        if (result.count == 0) {
            val uiTree = serviceManager.getUiTreeManager().getCurrentUiTree()
            val liteTree = createLiteTree(uiTree.rootNode)

            // Sử dụng actionType từ request để quyết định fallback
            val fallbackType = parseActionType(request.actionType)
            val fallbackResponse = createSmartFallbackResponse(result, liteTree, fallbackType, uiTree.captureTime)

            return createSuccessResponse(fallbackResponse)
        }

        return createSuccessResponse(result)
    }
    
    /**
     * POST /wait-for-element
     */
    fun waitForElement(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val request = parseRequestBody<WaitForElementRequest>(session)
            ?: return createErrorResponse(400, "INVALID_REQUEST", "Invalid request body")
        
        // This would need to be implemented as a suspending function
        // For now, return a simple response
        return createErrorResponse(501, "NOT_IMPLEMENTED", "Wait for element not yet implemented")
    }

    /**
     * Parse actionType từ string thành FallbackType tương ứng
     */
    private fun parseActionType(actionType: String?): FallbackType {
        return when (actionType?.lowercase()) {
            "click" -> FallbackType.CLICKABLE      // Muốn click -> trả về clickable elements
            "input" -> FallbackType.EDITABLE       // Muốn nhập -> trả về editable elements
            "scroll" -> FallbackType.SCROLLABLE    // Muốn scroll -> trả về scrollable elements
            "check" -> FallbackType.CHECKABLE      // Muốn check/toggle -> trả về checkable elements
            "read" -> FallbackType.TEXT            // Muốn đọc -> trả về text elements
            else -> FallbackType.CLICKABLE         // Default: clickable (most common action)
        }
    }

    /**
     * Tạo smart fallback response dựa trên loại được yêu cầu
     */
    private fun createSmartFallbackResponse(
        result: Any,
        liteTree: LiteTree,
        fallbackType: FallbackType,
        captureTime: Long
    ): Map<String, Any> {
        return when (fallbackType) {
            FallbackType.CLICKABLE -> mapOf(
                "elements" to emptyList<Any>(),
                "count" to 0,
                "fallback" to true,
                "fallbackType" to "clickable",
                "message" to "No elements found, returning clickable elements",
                "clickableElements" to liteTree.clickableElements,
                "totalClickable" to liteTree.clickableElements.size,
                "captureTime" to captureTime
            )

            FallbackType.EDITABLE -> mapOf(
                "elements" to emptyList<Any>(),
                "count" to 0,
                "fallback" to true,
                "fallbackType" to "editable",
                "message" to "No elements found, returning editable elements",
                "editableElements" to liteTree.editableElements,
                "totalEditable" to liteTree.editableElements.size,
                "captureTime" to captureTime
            )

            FallbackType.SCROLLABLE -> mapOf(
                "elements" to emptyList<Any>(),
                "count" to 0,
                "fallback" to true,
                "fallbackType" to "scrollable",
                "message" to "No elements found, returning scrollable elements",
                "scrollableElements" to liteTree.scrollableElements,
                "totalScrollable" to liteTree.scrollableElements.size,
                "captureTime" to captureTime
            )

            FallbackType.CHECKABLE -> mapOf(
                "elements" to emptyList<Any>(),
                "count" to 0,
                "fallback" to true,
                "fallbackType" to "checkable",
                "message" to "No elements found, returning checkable elements",
                "checkableElements" to liteTree.checkableElements,
                "switchElements" to liteTree.switchElements,
                "totalCheckable" to liteTree.checkableElements.size,
                "totalSwitch" to liteTree.switchElements.size,
                "captureTime" to captureTime
            )

            FallbackType.TEXT -> mapOf(
                "elements" to emptyList<Any>(),
                "count" to 0,
                "fallback" to true,
                "fallbackType" to "text",
                "message" to "No elements found, returning text elements",
                "textElements" to liteTree.textElements,
                "totalText" to liteTree.textElements.size,
                "captureTime" to captureTime
            )


        }
    }

    /**
     * Enum cho các loại fallback dựa trên action type
     */
    private enum class FallbackType {
        CLICKABLE, EDITABLE, SCROLLABLE, CHECKABLE, TEXT
    }

    /**
     * Tạo compact tree cho AI với thông tin gọn nhưng đầy đủ
     */
    private fun createCompactTree(rootNode: com.tomisakae.accessibilityserviceapi.domain.models.UiNode?): CompactTree {
        val clickableElements = mutableListOf<CompactElement>()
        val editableElements = mutableListOf<CompactElement>()
        val scrollableElements = mutableListOf<CompactElement>()
        val textElements = mutableListOf<CompactElement>()
        val structure = mutableListOf<StructureNode>()

        if (rootNode != null) {
            extractCompactElements(rootNode, clickableElements, editableElements, scrollableElements, textElements)
            buildStructure(rootNode, structure, 0)
        }

        return CompactTree(clickableElements, editableElements, scrollableElements, textElements, structure)
    }

    /**
     * Trích xuất elements cho compact tree
     */
    private fun extractCompactElements(
        node: com.tomisakae.accessibilityserviceapi.domain.models.UiNode,
        clickableElements: MutableList<CompactElement>,
        editableElements: MutableList<CompactElement>,
        scrollableElements: MutableList<CompactElement>,
        textElements: MutableList<CompactElement>
    ) {
        val element = CompactElement(
            id = node.id ?: "node_${node.bounds.centerX}_${node.bounds.centerY}",
            text = node.text?.take(50), // Giới hạn 50 ký tự
            contentDescription = node.contentDescription?.take(50),
            className = node.className?.substringAfterLast('.'), // Chỉ lấy tên class cuối
            bounds = mapOf(
                "centerX" to node.bounds.centerX,
                "centerY" to node.bounds.centerY,
                "width" to node.bounds.width,
                "height" to node.bounds.height
            ),
            states = mapOf(
                "clickable" to node.isClickable,
                "editable" to isEditableElement(node),
                "scrollable" to node.isScrollable,
                "enabled" to node.isEnabled,
                "checked" to node.isChecked,
                "selected" to node.isSelected,
                "focused" to node.isFocused
            )
        )

        // Phân loại elements
        if (node.isClickable && node.isEnabled) {
            clickableElements.add(element)
        }

        if (isEditableElement(node)) {
            editableElements.add(element)
        }

        if (node.isScrollable) {
            scrollableElements.add(element)
        }

        if (!node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank()) {
            textElements.add(element)
        }

        // Đệ quy cho children
        node.children.forEach { child ->
            extractCompactElements(child, clickableElements, editableElements, scrollableElements, textElements)
        }
    }

    /**
     * Tạo cấu trúc phân cấp gọn
     */
    private fun buildStructure(
        node: com.tomisakae.accessibilityserviceapi.domain.models.UiNode,
        structure: MutableList<StructureNode>,
        level: Int
    ) {
        // Chỉ thêm node quan trọng vào structure
        if (node.isClickable || isEditableElement(node) || node.isScrollable ||
            !node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank() ||
            node.children.isNotEmpty()) {

            val structureNode = StructureNode(
                level = level,
                type = when {
                    node.isClickable -> "clickable"
                    isEditableElement(node) -> "editable"
                    node.isScrollable -> "scrollable"
                    !node.text.isNullOrBlank() -> "text"
                    else -> "container"
                },
                label = node.text ?: node.contentDescription ?: node.className?.substringAfterLast('.') ?: "unknown",
                bounds = "${node.bounds.centerX},${node.bounds.centerY}",
                childCount = node.children.size
            )

            structure.add(structureNode)

            // Đệ quy cho children (giới hạn độ sâu)
            if (level < 3 && node.children.isNotEmpty()) {
                node.children.forEach { child ->
                    buildStructure(child, structure, level + 1)
                }
            }
        }
    }

    /**
     * Data classes cho compact tree
     */
    private data class CompactTree(
        val clickableElements: List<CompactElement>,
        val editableElements: List<CompactElement>,
        val scrollableElements: List<CompactElement>,
        val textElements: List<CompactElement>,
        val structure: List<StructureNode>
    )

    private data class CompactElement(
        val id: String,
        val text: String?,
        val contentDescription: String?,
        val className: String?,
        val bounds: Map<String, Int>,
        val states: Map<String, Boolean>
    )

    private data class StructureNode(
        val level: Int,
        val type: String,
        val label: String,
        val bounds: String,
        val childCount: Int
    )

    /**
     * Tạo lite tree chỉ chứa thông tin cần thiết
     */
    private fun createLiteTree(rootNode: com.tomisakae.accessibilityserviceapi.domain.models.UiNode?): LiteTree {
        val clickableElements = mutableListOf<LiteElement>()
        val textElements = mutableListOf<LiteElement>()
        val editableElements = mutableListOf<LiteElement>()
        val scrollableElements = mutableListOf<LiteElement>()
        val checkableElements = mutableListOf<LiteElement>()
        val switchElements = mutableListOf<LiteElement>()

        if (rootNode != null) {
            extractLiteElements(
                rootNode,
                clickableElements,
                textElements,
                editableElements,
                scrollableElements,
                checkableElements,
                switchElements
            )
        }

        return LiteTree(
            clickableElements,
            textElements,
            editableElements,
            scrollableElements,
            checkableElements,
            switchElements
        )
    }

    /**
     * Trích xuất elements quan trọng từ UI tree
     */
    private fun extractLiteElements(
        node: com.tomisakae.accessibilityserviceapi.domain.models.UiNode,
        clickableElements: MutableList<LiteElement>,
        textElements: MutableList<LiteElement>,
        editableElements: MutableList<LiteElement>,
        scrollableElements: MutableList<LiteElement>,
        checkableElements: MutableList<LiteElement>,
        switchElements: MutableList<LiteElement>
    ) {
        // Thêm vào clickable elements nếu có thể click
        if (node.isClickable && node.isEnabled) {
            clickableElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = true,
                    isEditable = false
                )
            )
        }

        // Thêm vào editable elements nếu có thể edit (input fields)
        if (isEditableElement(node)) {
            editableElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = node.isClickable,
                    isEditable = true
                )
            )
        }

        // Thêm vào text elements nếu có text hoặc content description
        if (!node.text.isNullOrBlank() || !node.contentDescription.isNullOrBlank()) {
            textElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = node.isClickable,
                    isEditable = isEditableElement(node)
                )
            )
        }

        // Scrollable elements
        if (node.isScrollable) {
            scrollableElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = node.isClickable,
                    isEditable = isEditableElement(node)
                )
            )
        }

        // Checkable elements (checkbox, radiobutton)
        if (node.isCheckable && !isSwitchElement(node)) {
            checkableElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = node.isClickable,
                    isEditable = isEditableElement(node)
                )
            )
        }

        // Switch elements
        if (isSwitchElement(node)) {
            switchElements.add(
                LiteElement(
                    id = node.id,
                    text = node.text,
                    contentDescription = node.contentDescription,
                    className = node.className,
                    bounds = node.bounds,
                    isClickable = node.isClickable,
                    isEditable = isEditableElement(node)
                )
            )
        }

        // Đệ quy cho children
        node.children.forEach { child ->
            extractLiteElements(child, clickableElements, textElements, editableElements, scrollableElements, checkableElements, switchElements)
        }
    }

    /**
     * Kiểm tra xem node có phải là editable element không (chỉ dựa trên className và properties)
     */
    private fun isEditableElement(node: com.tomisakae.accessibilityserviceapi.domain.models.UiNode): Boolean {
        val className = node.className?.lowercase() ?: ""

        return when {
            // EditText classes - chỉ check className
            className.contains("edittext") -> true
            className.contains("textinputedittext") -> true
            className.contains("autocompletetextview") -> true
            className.contains("textinputlayout") -> true

            // Focusable and enabled (potential input) - dựa trên properties
            node.isFocusable && node.isEnabled && !node.isClickable -> true

            else -> false
        }
    }

    /**
     * Kiểm tra xem node có phải là switch element không (chỉ dựa trên className)
     */
    private fun isSwitchElement(node: com.tomisakae.accessibilityserviceapi.domain.models.UiNode): Boolean {
        val className = node.className?.lowercase() ?: ""

        return when {
            className.contains("switch") -> true
            className.contains("togglebutton") -> true
            className.contains("compoundbutton") -> true
            else -> false
        }
    }

    /**
     * Data class cho lite tree
     */
    private data class LiteTree(
        val clickableElements: List<LiteElement>,
        val textElements: List<LiteElement>,
        val editableElements: List<LiteElement>,
        val scrollableElements: List<LiteElement>,
        val checkableElements: List<LiteElement>,
        val switchElements: List<LiteElement>
    )

    /**
     * Data class cho lite element
     */
    private data class LiteElement(
        val id: String?,
        val text: String?,
        val contentDescription: String?,
        val className: String?,
        val bounds: com.tomisakae.accessibilityserviceapi.domain.models.NodeBounds,
        val isClickable: Boolean,
        val isEditable: Boolean
    )
}
