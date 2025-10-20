package app.inv.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ItemRequest {
    @NotBlank(message = "物品名称不能为空")
    private String name;
    
    private UUID categoryId;
    
    private String brand;
    
    private String size;
    
    private String color;
    
    @NotNull(message = "购买价格不能为空")
    @DecimalMin(value = "0.0", message = "购买价格不能为负数")
    private BigDecimal purchasePrice;
    
    @NotNull(message = "购买日期不能为空")
    @PastOrPresent(message = "购买日期不能是未来日期")
    private LocalDate purchaseDate;
    
    private String location;
    
    private String images;
}
