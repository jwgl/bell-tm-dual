package cn.edu.bnuz.bell.dualdegree


class AgreementCommand {

    Long          id
    String        agreementName
    Integer       universityId
    String        memo

    List<Item>      addedItems
    List<Integer>   removedItems

    class Item {
        /**
         * 年级专业
         */
        Integer id
        List<Integer>   coMajors
    }

}
