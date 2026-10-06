/**
 * Bộ từ điển đa ngôn ngữ (i18n) thuần JavaScript cho hệ thống TFUS
 * Hỗ trợ chuyển đổi tức thì giữa Tiếng Việt (VI) và Tiếng Anh (EN).
 */
const I18N = {
    currentLang: localStorage.getItem('tfus_lang') || 'vi',

    dict: {
        vi: {
            // Header & App
            app_title: "🌊 TFUS Khai phá Luồng Dữ liệu",
            app_subtitle: "Khai phá mẫu tiện ích mờ theo thời gian với mô hình Cửa sổ trượt (Java 17+ thuần)",
            btn_run_mine: "⚡ Chạy Thuật Toán",
            btn_mining: "Đang chạy thuật toán...",
            btn_run_verify: "🧪 Đối chiếu Chạy tay",
            btn_verifying: "Đang đối chiếu...",
            handrun_available: " (Có dữ liệu đối chiếu)",

            // Tabs
            tab_overview: "Tổng quan & Cấu hình",
            tab_results: "Kết quả Khai phá",
            tab_steps: "Trace các Bước",
            tab_verify: "Báo cáo Đối chiếu",
            tab_theory: "Lý thuyết & Kiến trúc",

            // Tab 1: Overview & Config
            card_config_title: "Cấu hình Khai phá",
            label_window_size: "Kích thước cửa sổ trượt (ws - số lô mỗi cửa sổ):",
            label_threshold_delta: "Ngưỡng tiện ích tối thiểu (δ):",
            label_pruning_strategy: "Chiến lược cắt tỉa:",
            opt_pruning_rub: "Cận trên hiệu chỉnh (Cắt tỉa CT13 - Revised Upper Bound)",
            opt_pruning_none: "Không cắt tỉa (Sinh đầy đủ mọi ứng viên - Full Search)",
            card_items_title: "Tiện ích Ngoại (EU) & Số lượng Cực đại (M)",
            th_item: "Vật phẩm (Item)",
            th_eu: "Tiện ích Ngoại (EU)",
            th_m: "Số lượng Cực đại (M)",
            card_txns_title: "Các Giao dịch theo Luồng (Bảng 1)",
            th_tid: "Mã GD (TID)",
            th_batch: "Lô (Batch)",
            th_period: "Chu kỳ (Period)",
            th_items_qty: "Các vật phẩm (Số lượng nội bộ IU)",

            // Tab 2: Results
            results_empty: "Bấm <strong>⚡ Chạy Thuật Toán</strong> để thực thi thuật toán và hiển thị tập mẫu kết quả.",
            stat_windows_mined: "Cửa sổ đã khai phá",
            stat_total_patterns: "Tổng mẫu kết quả",
            stat_threshold: "Ngưỡng (δ)",
            card_window_title: "Kết quả Cửa sổ W{num} ({count} Mẫu kết quả)",
            th_rank: "#",
            th_pattern: "Mẫu mờ (Pattern)",
            th_tfur: "Tỷ lệ tFUR (CT12)",
            th_start_period: "Chu kỳ bắt đầu (PS)",
            th_status: "Trạng thái",
            badge_result: "Mẫu kết quả",

            // Tab 3: Step Trace
            trace_empty: "Bấm <strong>⚡ Chạy Thuật Toán</strong> để ghi lại nhật ký trace từng bước thực thi thuật toán.",
            trace_batch_arrived: "📦 Lô B{batch} đến (Cửa sổ #{window})",
            trace_txn_scanned: "🔍 Đã quét {tid} (Lô B{batch}, Chu kỳ P{period})",
            trace_batch_removed: "🗑️ Lô B{batch} bị loại bỏ (Trượt cửa sổ)",
            trace_batch_removed_desc: "Đã xóa {count} mục entry khỏi các TFUS-list toàn cục.",
            trace_restructure: "⚙️ Tái cấu trúc Cửa sổ W{window}",
            trace_sigma_tfu: "Tổng tiện ích mờ chu kỳ ΣTFU(PS):",
            trace_pruned_items: "Các item bị cắt tỉa do không tiềm năng:",
            trace_processing_order: "Thứ tự xử lý (FUUB tăng dần):",
            trace_window_mined: "✅ Khai phá xong Cửa sổ W{window}",
            trace_window_mined_desc: "Đã phát hiện <strong>{count}</strong> mẫu tiện ích mờ thời gian cao.",

            // Tab 4: Verification
            verify_empty: "Bấm <strong>🧪 Đối chiếu Chạy tay</strong> để so khớp từng số liệu với kết quả bài báo.",
            stat_handrun_match: "Độ khớp Chạy tay",
            stat_exact_pass: "Khớp chính xác",
            stat_rounding_tol: "Trong ngưỡng làm tròn",
            stat_mismatch: "Lệch số liệu",
            card_verify_detail: "Chi tiết các phép kiểm chứng ({count} Phép đối chiếu)",
            th_group: "Nhóm kiểm tra",
            th_metric: "Tên đại lượng",
            th_expected: "Số liệu Chạy tay",
            th_actual: "Thuật toán tính ra",
            th_result: "Kết quả",
            filter_search_placeholder: "🔍 Tìm kiếm trong 531 phép kiểm tra (VD: W1, W2, B.M, FU, tFUR)...",
            filter_all_status: "Tất cả trạng thái",
            filter_pass_only: "Khớp chính xác (PASS)",
            filter_rounding_only: "Trong ngưỡng làm tròn (ROUNDING)",
            filter_mismatch_only: "Lệch số liệu (MISMATCH)",

            // Tab 5: Theory & Architecture
            card_formulas_title: "14 Cốt lõi Công thức Toán học (CT1 - CT14)",
            th_formula_code: "Ký hiệu & Tên",
            th_formula_math: "Công thức Toán học",
            th_formula_desc: "Ý nghĩa & Mục đích trong TFUS",
            th_formula_stage: "Giai đoạn Áp dụng",
            card_patterns_title: "Các Mẫu Thiết kế (Design Patterns) đã Triển khai",
            th_dp_name: "Mẫu thiết kế",
            th_dp_class: "Lớp triển khai trong Java",
            th_dp_role: "Vai trò & Trách nhiệm",

            // Formulas translation
            ct1_name: "Tiện ích thực",
            ct1_desc: "Tiện ích thực tế của item i_n trong giao dịch T_m = số lượng nội bộ (IU) × lợi nhuận đơn vị (EU).",
            ct1_stage: "Quét giao dịch (Bước 1 & Bước 3.2)",

            ct2_name: "Giá trị mờ",
            ct2_desc: "Giá trị mờ đại diện mức độ thuộc về nhãn ngôn ngữ t ∈ {L, M, H} qua hàm thành viên tam giác/hình thang.",
            ct2_stage: "Mờ hóa dữ liệu (Bước 1 & Phụ lục A)",

            ct3_name: "Giá trị mờ nhỏ nhất",
            ct3_desc: "Giá trị mờ nhỏ nhất của mẫu X trong giao dịch T_m (chuẩn phép giao mờ T-norm minimum).",
            ct3_stage: "Khởi tạo entry & Giao danh sách",

            ct4_name: "Tiện ích mờ giao dịch",
            ct4_desc: "Tiện ích mờ của mẫu X trong một giao dịch = độ mờ tối thiểu nhân với tổng tiện ích thực của các item.",
            ct4_stage: "Thêm entry vào TFUS-list",

            ct5_name: "Tiện ích mờ cửa sổ",
            ct5_desc: "Tổng tiện ích mờ tích lũy của mẫu X trong toàn bộ cửa sổ trượt hiện hành W_k.",
            ct5_stage: "TFUSList.fu(), dùng xét mẫu kết quả",

            ct6_name: "Tiện ích mờ giao dịch",
            ct6_desc: "Tổng tiện ích mờ qua mọi nhãn của tất cả item trong T_m; làm cơ sở tính mẫu số chu kỳ.",
            ct6_stage: "Lưu vào UBTable khi quét giao dịch",

            ct7_name: "Tiện ích mờ cực đại",
            ct7_desc: "Tiện ích mờ giao dịch cực đại: chỉ lấy nhãn có Fv lớn nhất cho mỗi item để tránh cộng trùng tiện ích.",
            ct7_stage: "Cộng dồn vào FUUB của từng item",

            ct8_name: "Cận trên tiện ích mờ",
            ct8_desc: "Cận trên tiện ích mờ của item: tổng MTFU của các giao dịch chứa item trong cửa sổ W_k.",
            ct8_stage: "Quét giao dịch & Cập nhật lô",

            ct9_name: "Tỷ lệ cận trên mờ",
            ct9_desc: "Tỷ lệ cận trên tiện ích mờ thời gian: loại bỏ sớm các item không tiềm năng nếu TFUUBR < δ.",
            ct9_stage: "Tái cấu trúc (Restructure - Bước 2)",

            ct10_name: "Tiện ích mờ còn lại",
            ct10_desc: "Tổng tiện ích các item đứng sau theo thứ tự xử lý; dùng bảng tạm tránh cộng trùng cùng item.",
            ct10_stage: "Duyệt ngược danh sách tái cấu trúc",

            ct11_name: "Trừ tiện ích khi trượt",
            ct11_desc: "Cập nhật trừ dần tiện ích mờ khi lô cũ nhất B_oldest trượt ra ngoài cửa sổ mà không cần quét lại từ đầu.",
            ct11_stage: "Trượt cửa sổ (Bước 3.1 - Xóa lô cũ)",

            ct12_name: "Tỷ lệ tiện ích mờ thời gian",
            ct12_desc: "Tiêu chuẩn xác nhận mẫu kết quả nếu tFUR ≥ δ (Mẫu tiện ích mờ thời gian cao).",
            ct12_stage: "Đánh giá kết quả (Algorithm 2)",

            ct13_name: "Tỷ lệ cận trên hiệu chỉnh",
            ct13_desc: "Điều kiện cắt tỉa nhánh đệ quy (Lemma 2): nếu rTFUUBR < δ thì cắt tỉa toàn bộ nhánh con.",
            ct13_stage: "Cắt tỉa nhánh (Pruning Strategy)",

            ct14_name: "Ghép danh sách điều kiện",
            ct14_desc: "Cấu trúc entry khi kết hợp hai danh sách X và Y trong cùng giao dịch, trừ phần tiền tố qua bảng PUTable.",
            ct14_stage: "Mở rộng mẫu (Pattern Extension)",

            // Design Patterns translation
            dp_template_name: "Template Method",
            dp_template_role: "Đóng khung vòng đời luồng cửa sổ trượt (xóa lô cũ, chèn lô mới, tái cấu trúc, khai thác).",
            dp_strategy_name: "Strategy",
            dp_strategy_role: "Thay đổi linh hoạt hàm thành viên mờ (Tam giác/Hình thang) và chiến lược cắt tỉa (Pruning).",
            dp_observer_name: "Observer",
            dp_observer_role: "Phát và bắt các sự kiện trace chi tiết (TransactionScanned, RestructureCompleted, ...).",
            dp_memento_name: "Memento",
            dp_memento_role: "Đóng băng bản chụp trạng thái bất biến của TFUS-list sau tái cấu trúc để phục vụ audit/UI.",
            dp_command_name: "Command",
            dp_command_role: "Đóng gói các endpoint HTTP API thành các đối tượng Command xử lý độc lập.",
            dp_facade_name: "Facade",
            dp_facade_role: "Điểm điều phối chung, giao tiếp thống nhất cho cả giao diện CLI, Web và mô-đun Đối chiếu.",

            // Footer
            footer_text: "Hệ thống Khai phá Luồng TFUS • Thư viện Tiêu chuẩn Java 17+ (Không Framework, Không Spring, Không Thư viện JSON ngoài)"
        },

        en: {
            // Header & App
            app_title: "🌊 TFUS Stream Miner",
            app_subtitle: "Temporal Fuzzy Utility Pattern Mining under Moving Sliding Windows (Pure Java 17+)",
            btn_run_mine: "⚡ Run Mining",
            btn_mining: "Mining...",
            btn_run_verify: "🧪 Verify Handrun",
            btn_verifying: "Verifying...",
            handrun_available: " (Handrun Available)",

            // Tabs
            tab_overview: "Overview & Config",
            tab_results: "Mining Results",
            tab_steps: "Step Trace",
            tab_verify: "Verification Report",
            tab_theory: "Theory & Architecture",

            // Tab 1: Overview & Config
            card_config_title: "Mining Configuration",
            label_window_size: "Window Size (ws - batches per window):",
            label_threshold_delta: "Minimum Utility Threshold (δ):",
            label_pruning_strategy: "Pruning Strategy:",
            opt_pruning_rub: "Revised Upper Bound (CT13 Pruning)",
            opt_pruning_none: "No Pruning (Generate All Full Candidates)",
            card_items_title: "External Utility (EU) & Max Quantity (M)",
            th_item: "Item",
            th_eu: "External Utility (EU)",
            th_m: "Max Quantity (M)",
            card_txns_title: "Stream Transactions (Table 1)",
            th_tid: "TID",
            th_batch: "Batch",
            th_period: "Period",
            th_items_qty: "Items (Internal Quantity IU)",

            // Tab 2: Results
            results_empty: "Click <strong>⚡ Run Mining</strong> to execute algorithm and display patterns.",
            stat_windows_mined: "Windows Mined",
            stat_total_patterns: "Total Patterns",
            stat_threshold: "Threshold (δ)",
            card_window_title: "Window W{num} Results ({count} Patterns)",
            th_rank: "#",
            th_pattern: "Pattern",
            th_tfur: "tFUR Ratio (CT12)",
            th_start_period: "Start Period (PS)",
            th_status: "Status",
            badge_result: "Result Pattern",

            // Tab 3: Step Trace
            trace_empty: "Click <strong>⚡ Run Mining</strong> to record execution trace steps.",
            trace_batch_arrived: "📦 Batch B{batch} Arrived (Window #{window})",
            trace_txn_scanned: "🔍 Scanned {tid} (Batch B{batch}, Period P{period})",
            trace_batch_removed: "🗑️ Batch B{batch} Removed (Window Shift)",
            trace_batch_removed_desc: "Removed {count} entries from global TFUS-lists.",
            trace_restructure: "⚙️ Restructure Window W{window}",
            trace_sigma_tfu: "Period Sum Fuzzy Utility ΣTFU(PS):",
            trace_pruned_items: "Pruned non-promising items:",
            trace_processing_order: "Processing Order (FUUB ascending):",
            trace_window_mined: "✅ Window W{window} Mined",
            trace_window_mined_desc: "Discovered <strong>{count}</strong> high temporal fuzzy utility patterns.",

            // Tab 4: Verification
            verify_empty: "Click <strong>🧪 Verify Handrun</strong> to compare with hand-calculated paper reference.",
            stat_handrun_match: "Handrun Match",
            stat_exact_pass: "Exact Pass",
            stat_rounding_tol: "Rounding Tolerance",
            stat_mismatch: "Mismatch",
            card_verify_detail: "Verification Checks Detail ({count} Checks)",
            th_group: "Check Group",
            th_metric: "Metric Name",
            th_expected: "Handrun Expected",
            th_actual: "Algorithm Actual",
            th_result: "Status",
            filter_search_placeholder: "🔍 Search across 531 checks (e.g., W1, W2, B.M, FU, tFUR)...",
            filter_all_status: "All Statuses",
            filter_pass_only: "Exact Pass (PASS)",
            filter_rounding_only: "Rounding Tolerance (ROUNDING)",
            filter_mismatch_only: "Mismatch (MISMATCH)",

            // Tab 5: Theory & Architecture
            card_formulas_title: "14 Core Mathematical Formulas (CT1 - CT14)",
            th_formula_code: "Formula & Name",
            th_formula_math: "Mathematical Expression",
            th_formula_desc: "Meaning & Purpose in TFUS",
            th_formula_stage: "Application Stage",
            card_patterns_title: "Applied Design Patterns",
            th_dp_name: "Design Pattern",
            th_dp_class: "Implementation Class",
            th_dp_role: "Role & Responsibility",

            // Formulas translation
            ct1_name: "Crisp Utility",
            ct1_desc: "Actual utility of item i_n in transaction T_m = internal quantity (IU) × unit profit (EU).",
            ct1_stage: "Transaction Scan (Step 1 & Step 3.2)",

            ct2_name: "Fuzzy Value",
            ct2_desc: "Fuzzy membership value for linguistic term t ∈ {L, M, H} via triangular/trapezoidal membership functions.",
            ct2_stage: "Fuzzification (Step 1 & Appendix A)",

            ct3_name: "Minimum Fuzzy Value",
            ct3_desc: "Smallest fuzzy value of pattern X in transaction T_m (standard fuzzy T-norm minimum).",
            ct3_stage: "Entry Creation & List Intersection",

            ct4_name: "Transaction Fuzzy Utility",
            ct4_desc: "Fuzzy utility of pattern X in one transaction = minimum fuzzy value multiplied by sum of crisp utilities.",
            ct4_stage: "TFUS-list Entry Addition",

            ct5_name: "Window Fuzzy Utility",
            ct5_desc: "Cumulative fuzzy utility of pattern X across all batches in current sliding window W_k.",
            ct5_stage: "TFUSList.fu(), used for pattern evaluation",

            ct6_name: "Transaction Fuzzy Utility",
            ct6_desc: "Total fuzzy utility of all linguistic terms of all items in T_m; serves as temporal denominator base.",
            ct6_stage: "Stored in UBTable during scan",

            ct7_name: "Max-term Fuzzy Utility",
            ct7_desc: "Max-term transaction fuzzy utility: picks highest Fv term per item to avoid duplicate utility addition.",
            ct7_stage: "Accumulated into FUUB per item",

            ct8_name: "Fuzzy Utility Upper Bound",
            ct8_desc: "Upper bound of item i_n.t: sum of MTFU across all transactions containing the item in window W_k.",
            ct8_stage: "Transaction Scan & Window Shift",

            ct9_name: "Temporal FUUB Ratio",
            ct9_desc: "Temporal fuzzy utility upper bound ratio: filters out unpromising singleton items if TFUUBR < δ.",
            ct9_stage: "Restructure (Step 2)",

            ct10_name: "Remaining Fuzzy Utility",
            ct10_desc: "Remaining fuzzy utility of subsequent items in processing order; uses temp table to avoid item duplication.",
            ct10_stage: "Reverse Traversal during Restructure",

            ct11_name: "Batch Expiration Decrement",
            ct11_desc: "Incrementally subtracts fuzzy utility of expired batch B_oldest without re-scanning whole stream.",
            ct11_stage: "Sliding Window Shift (Step 3.1)",

            ct12_name: "Temporal FU Ratio (tFUR)",
            ct12_desc: "Temporal fuzzy utility ratio: criteria to qualify as high temporal fuzzy utility pattern if tFUR ≥ δ.",
            ct12_stage: "Pattern Evaluation (Algorithm 2)",

            ct13_name: "Revised Upper Bound Ratio",
            ct13_desc: "Branch pruning criterion (Lemma 2): if rTFUUBR < δ, prune entire recursive subtree.",
            ct13_stage: "Branch Pruning (Pruning Strategy)",

            ct14_name: "Conditional TFUS-List",
            ct14_desc: "Entry structure when intersecting lists X and Y in transaction T_m, subtracting prefix utility via PUTable.",
            ct14_stage: "Pattern Extension (Recursive Mining)",

            // Design Patterns translation
            dp_template_name: "Template Method",
            dp_template_role: "Defines sliding-window stream lifecycle (remove oldest batch, insert batch, restructure, mine).",
            dp_strategy_name: "Strategy",
            dp_strategy_role: "Interchangeable membership functions (Triangular/Trapezoidal) and pruning policies.",
            dp_observer_name: "Observer",
            dp_observer_role: "Publishes and captures fine-grained mining trace events (TransactionScanned, RestructureCompleted, etc.).",
            dp_memento_name: "Memento",
            dp_memento_role: "Freezes immutable snapshot of TFUS-lists after restructure for audit and inspection.",
            dp_command_name: "Command",
            dp_command_role: "Encapsulates HTTP API endpoint execution into dedicated command objects.",
            dp_facade_name: "Facade",
            dp_facade_role: "Unified entry coordinator for CLI, Web API, and Automated Verification runs.",

            // Footer
            footer_text: "TFUS Stream Mining System • Pure Java 17+ Standard Library (No Spring, No Tomcat, No External JSON Lib)"
        }
    },

    t(key, params = {}) {
        const lang = this.currentLang;
        let text = (this.dict[lang] && this.dict[lang][key]) || (this.dict['vi'] && this.dict['vi'][key]) || key;
        for (const [k, v] of Object.entries(params)) {
            text = text.replace(new RegExp(`\\{${k}\\}`, 'g'), v);
        }
        return text;
    },

    setLanguage(lang) {
        if (!this.dict[lang]) return;
        this.currentLang = lang;
        localStorage.setItem('tfus_lang', lang);

        // Cập nhật nút active
        document.querySelectorAll('.lang-btn').forEach(btn => {
            if (btn.getAttribute('data-lang') === lang) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        // Cập nhật toàn bộ các phần tử có data-i18n
        document.querySelectorAll('[data-i18n]').forEach(el => {
            const key = el.getAttribute('data-i18n');
            const translation = this.t(key);
            if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                el.value = translation;
            } else {
                el.innerHTML = translation;
            }
        });

        // Cập nhật title attributes
        document.querySelectorAll('[data-i18n-title]').forEach(el => {
            const key = el.getAttribute('data-i18n-title');
            el.setAttribute('title', this.t(key));
        });

        // Trigger custom event để re-render dynamic content (results, trace, verification)
        document.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang } }));
    }
};
