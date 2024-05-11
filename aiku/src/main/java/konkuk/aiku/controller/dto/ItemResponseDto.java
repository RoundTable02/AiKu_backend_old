package konkuk.aiku.controller.dto;

import konkuk.aiku.domain.EventStatus;
import konkuk.aiku.domain.ItemCategory;
import konkuk.aiku.domain.item.Item;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class ItemResponseDto {
    private Long itemId;
    private String itemName;
    private ItemCategory itemCategory;
    private int price;
    private int eventPrice;
    private EventStatus eventStatus;

    public static ItemResponseDto toDto(Item item) {
        return ItemResponseDto.builder()
                .itemId(item.getId())
                .itemName(item.getItemName())
                .itemCategory(item.getItemCategory())
                .price(item.getPrice())
                .eventPrice(item.getEventPrice())
                .eventStatus(item.getEventStatus())
                .build();
    }
}
