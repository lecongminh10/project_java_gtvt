# 📦 Hướng dẫn CRUD - Spring Boot + Thymeleaf

> Dự án sử dụng kiến trúc phân tầng: **Controller → Service → Repository → Database**  
> Mọi module mới đều làm theo đúng quy trình này.

---

## 🗂️ Cấu trúc thư mục cần tạo

Ví dụ tạo module **Product**, cần tạo đủ các file sau:

```
src/main/java/com/example/project/
├── entity/
│   └── Product.java                        ← 1. Entity (ánh xạ bảng DB)
├── dto/
│   └── ProductDTO.java                     ← 2. DTO (dữ liệu trao đổi)
├── repository/
│   └── ProductRepository.java              ← 3. Repository (truy vấn DB)
├── service/
│   ├── ProductService.java                 ← 4. Service Interface
│   └── impl/
│       └── ProductServiceImpl.java         ← 5. Service Implementation
└── controller/
    └── admin/
        └── AdminProductController.java     ← 6. Controller (nhận HTTP request)

src/main/resources/templates/admin/product/
├── list.html                               ← 7. Trang danh sách
└── form.html                               ← 8. Form thêm / sửa
```

---

## BƯỚC 1 — Entity

> Ánh xạ class Java → bảng trong MySQL. Spring tự tạo bảng nếu cấu hình `ddl-auto=update`.

```java
// src/main/java/com/example/project/entity/Product.java

@Entity
@Table(name = "products")
@Data               // Lombok: tự sinh getter/setter/toString
@NoArgsConstructor  // Lombok: constructor không tham số
@AllArgsConstructor // Lombok: constructor đầy đủ
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID tự tăng
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    private Integer stock; // Số lượng tồn kho
}
```

---

## BƯỚC 2 — DTO

> Đối tượng truyền dữ liệu giữa Controller ↔ Service. **Không dùng Entity trực tiếp** để tránh lộ thông tin nhạy cảm.

```java
// src/main/java/com/example/project/dto/ProductDTO.java

@Data
public class ProductDTO {
    private Long    id;
    private String  name;
    private String  description;
    private Double  price;
    private Integer stock;
}
```

---

## BƯỚC 3 — Repository

> Giao tiếp với DB. **Không cần viết SQL** — Spring Data JPA tự sinh câu lệnh theo tên phương thức.

```java
// src/main/java/com/example/project/repository/ProductRepository.java

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tìm theo tên (không phân biệt hoa thường)
    List<Product> findByNameContainingIgnoreCase(String keyword);

    // Tìm theo khoảng giá
    List<Product> findByPriceBetween(Double min, Double max);

    // Kiểm tra tên đã tồn tại chưa
    boolean existsByNameIgnoreCase(String name);
}
```

**Các hàm JpaRepository cho sẵn:**

| Phương thức | Chức năng |
|---|---|
| `findAll()` | Lấy tất cả |
| `findById(id)` | Tìm theo ID → `Optional<T>` |
| `save(entity)` | Thêm mới hoặc cập nhật |
| `deleteById(id)` | Xóa theo ID |
| `existsById(id)` | Kiểm tra tồn tại |
| `count()` | Đếm tổng |

---

## BƯỚC 4 — Service Interface

> Khai báo các phương thức nghiệp vụ. Kế thừa `BaseService` để có sẵn CRUD.

```java
// src/main/java/com/example/project/service/ProductService.java

public interface ProductService extends BaseService<ProductDTO, Long> {

    // Thêm các nghiệp vụ đặc thù của Product (nếu có)
    List<ProductDTO> findByNameContaining(String keyword);
    boolean existsByName(String name);
}
```

**`BaseService<DTO, ID>` đã có sẵn các nhóm:**

| Nhóm | Phương thức |
|---|---|
| **LIST** | `findAll()`, `findAll(Sort)`, `findAll(Pageable)`, `findAllById(ids)` |
| **FIND** | `findById(id)` → Optional, `getById(id)` → ném exception nếu không có |
| **CREATE** | `create(dto)`, `createAll(dtos)` |
| **UPDATE** | `update(id, dto)`, `save(dto)` |
| **DELETE** | `deleteById(id)`, `deleteAllById(ids)`, `deleteAll()` |
| **CHECK** | `existsById(id)`, `count()` |

---

## BƯỚC 5 — Service Implementation

> Implement logic thực tế. Kế thừa `AbstractBaseService` — **không phải viết lại CRUD**.

```java
// src/main/java/com/example/project/service/impl/ProductServiceImpl.java

@Service
public class ProductServiceImpl
        extends AbstractBaseService<Product, ProductDTO, Long>
        implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    // ── 3 phương thức bắt buộc phải override ────────────────────────

    @Override
    protected JpaRepository<Product, Long> getRepository() {
        return productRepository;
    }

    @Override
    protected ProductDTO toDTO(Product entity) {
        ProductDTO dto = new ProductDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPrice(entity.getPrice());
        dto.setStock(entity.getStock());
        return dto;
    }

    @Override
    protected Product toEntity(ProductDTO dto) {
        Product p = new Product();
        p.setId(dto.getId());
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setStock(dto.getStock());
        return p;
    }

    // ── Override mergeEntity khi UPDATE (chỉ sửa field cần thiết) ───

    @Override
    protected Product mergeEntity(Product existing, ProductDTO dto) {
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setStock(dto.getStock());
        return existing; // Giữ nguyên ID, không tạo entity mới
    }

    // ── Nghiệp vụ đặc thù ───────────────────────────────────────────

    @Override
    public List<ProductDTO> findByNameContaining(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByNameIgnoreCase(name);
    }
}
```

---

## BƯỚC 6 — Controller

> Nhận HTTP request → gọi Service → trả dữ liệu về View (HTML).

```java
// src/main/java/com/example/project/controller/admin/AdminProductController.java

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    // LIST — GET /admin/products
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("pageTitle", "Sản phẩm");
        return "admin/product/list";
    }

    // FORM TẠO MỚI — GET /admin/products/create
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new ProductDTO()); // DTO rỗng cho form
        model.addAttribute("pageTitle", "Thêm sản phẩm");
        return "admin/product/form";
    }

    // LƯU MỚI — POST /admin/products/create
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO dto,
                         RedirectAttributes redirectAttributes) {
        productService.create(dto);
        redirectAttributes.addFlashAttribute("success", "Tạo sản phẩm thành công!");
        return "redirect:/admin/products";
    }

    // FORM SỬA — GET /admin/products/{id}/edit
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("pageTitle", "Sửa sản phẩm");
        return "admin/product/form";
    }

    // LƯU SỬA — POST /admin/products/{id}/edit
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute ProductDTO dto,
                         RedirectAttributes redirectAttributes) {
        productService.update(id, dto);
        redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        return "redirect:/admin/products";
    }

    // XÓA — POST /admin/products/{id}/delete
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        return "redirect:/admin/products";
    }
}
```

---

## BƯỚC 7 — list.html (Trang danh sách)

```html
<!-- templates/admin/product/list.html -->
<div layout:decorate="~{admin/layout/admin-layout}">
<div layout:fragment="content">

    <!-- Thông báo flash -->
    <div th:if="${success}" class="alert alert-success" th:text="${success}"></div>

    <!-- Header trang -->
    <div class="page-header d-flex justify-content-between align-items-center">
        <h1>Sản phẩm</h1>
        <a th:href="@{/admin/products/create}" class="btn-primary-solid">
            <i class="fas fa-plus"></i> Thêm sản phẩm
        </a>
    </div>

    <!-- Bảng dữ liệu -->
    <div class="card">
        <table class="table-admin">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Tên sản phẩm</th>
                    <th>Giá</th>
                    <th>Tồn kho</th>
                    <th class="text-end">Hành động</th>
                </tr>
            </thead>
            <tbody>
                <!-- th:each = vòng lặp qua danh sách từ model -->
                <tr th:each="p : ${products}">
                    <td th:text="${p.id}"></td>
                    <td th:text="${p.name}"></td>
                    <td th:text="${p.price} + 'đ'"></td>
                    <td th:text="${p.stock}"></td>
                    <td class="text-end">
                        <!-- Nút Sửa -->
                        <a th:href="@{/admin/products/{id}/edit(id=${p.id})}" class="btn-icon">
                            <i class="fas fa-pencil"></i>
                        </a>
                        <!-- Nút Xóa (dùng form POST vì HTML không hỗ trợ DELETE) -->
                        <form th:action="@{/admin/products/{id}/delete(id=${p.id})}"
                              method="post" style="display:inline;"
                              onsubmit="return confirm('Xóa sản phẩm này?')">
                            <button type="submit" class="btn-icon danger">
                                <i class="fas fa-trash"></i>
                            </button>
                        </form>
                    </td>
                </tr>
                <!-- Danh sách rỗng -->
                <tr th:if="${#lists.isEmpty(products)}">
                    <td colspan="5" class="text-center" style="padding:2rem; color:#64748b;">
                        Chưa có sản phẩm nào.
                        <a th:href="@{/admin/products/create}">Thêm mới</a>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

</div>
</div>
```

---

## BƯỚC 8 — form.html (Dùng chung Thêm + Sửa)

```html
<!-- templates/admin/product/form.html -->
<div layout:decorate="~{admin/layout/admin-layout}">
<div layout:fragment="content">

    <!--
        Nếu product.id == null → action = /create  (THÊM MỚI)
        Nếu product.id != null → action = /{id}/edit (SỬA)
        Dùng 1 form chung cho cả 2 trường hợp!
    -->
    <form th:action="${product.id == null}
                      ? @{/admin/products/create}
                      : @{/admin/products/{id}/edit(id=${product.id})}"
          th:object="${product}"
          method="post"
          style="max-width: 600px;">

        <div class="form-input-wrap">
            <label class="form-label-clean">Tên sản phẩm *</label>
            <!-- th:field="*{name}" bind input với ProductDTO.name -->
            <input type="text" class="form-control-clean" th:field="*{name}" required>
        </div>

        <div class="form-input-wrap">
            <label class="form-label-clean">Giá (đồng) *</label>
            <input type="number" class="form-control-clean" th:field="*{price}" required>
        </div>

        <div class="form-input-wrap">
            <label class="form-label-clean">Số lượng tồn kho</label>
            <input type="number" class="form-control-clean" th:field="*{stock}">
        </div>

        <div class="form-input-wrap">
            <label class="form-label-clean">Mô tả</label>
            <textarea class="form-control-clean" th:field="*{description}" rows="4"></textarea>
        </div>

        <div class="d-flex gap-2">
            <button type="submit" class="btn-primary-solid">
                <i class="fas fa-save"></i>
                <!-- Tự đổi chữ nút theo ngữ cảnh -->
                <span th:text="${product.id == null} ? 'Tạo mới' : 'Lưu thay đổi'"></span>
            </button>
            <a th:href="@{/admin/products}" class="btn-ghost">Hủy</a>
        </div>

    </form>

</div>
</div>
```

---

## 🗺️ Bản đồ URL đầy đủ

| URL | Method | Chức năng |
|---|---|---|
| `/admin/products` | GET | Xem danh sách |
| `/admin/products/create` | GET | Hiển thị form thêm mới |
| `/admin/products/create` | POST | Lưu bản ghi mới vào DB |
| `/admin/products/{id}/edit` | GET | Hiển thị form sửa |
| `/admin/products/{id}/edit` | POST | Lưu thay đổi vào DB |
| `/admin/products/{id}/delete` | POST | Xóa bản ghi |

---

## ⚡ Checklist nhanh khi thêm module mới

```
□ 1. Entity       → entity/XxxYyy.java
□ 2. DTO          → dto/XxxYyyDTO.java
□ 3. Repository   → repository/XxxYyyRepository.java  (extends JpaRepository)
□ 4. Service      → service/XxxYyyService.java         (extends BaseService)
□ 5. ServiceImpl  → service/impl/XxxYyyServiceImpl.java (extends AbstractBaseService)
□ 6. Controller   → controller/admin/AdminXxxYyyController.java
□ 7. list.html    → templates/admin/xxxyyyy/list.html
□ 8. form.html    → templates/admin/xxxyyyy/form.html
□ 9. Sidebar      → Thêm link trong fragments/sidebar.html
```

> 💡 **Mẹo:** Copy toàn bộ từ module `Category` đã có → tìm/thay thế tên → chỉnh field. Nhanh hơn nhiều so với viết từ đầu!
