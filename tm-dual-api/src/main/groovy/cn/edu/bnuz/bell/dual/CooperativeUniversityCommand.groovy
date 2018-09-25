package cn.edu.bnuz.bell.dual

class CooperativeUniversityCommand {
    Long id
    String shortName
    String nameCn
    String nameEn
    Integer regionId

    List<Item> addedItems
    List<String> removedItems

    class Item {
        /**
         * 英文缩写
         */
        Long id
        String nameCn
        String nameEn
        String bachelor
    }
}
