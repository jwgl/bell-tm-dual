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
         * 校内专业
         */
        String id
        Integer startedGrade
        Integer endedGrade
        List<Integer>   coMajors
    }

}
