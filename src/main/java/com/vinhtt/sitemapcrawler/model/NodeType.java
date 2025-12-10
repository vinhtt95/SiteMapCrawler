package com.vinhtt.sitemapcrawler.model;

/**
 * Enumeration representing the type of a node in the site map graph.
 *
 * @author vinhtt
 * @version 1.4
 */
public enum NodeType {
    INTERNAL, // Đã truy cập thành công
    EXTERNAL, // Link ra ngoài domain
    GROUPED,  // Nhóm các link giống nhau
    PENDING   // [NEW] Link tìm thấy nhưng chưa truy cập (Chờ user chọn)
}