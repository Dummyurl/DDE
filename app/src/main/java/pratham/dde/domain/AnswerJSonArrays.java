package pratham.dde.domain;

public class AnswerJSonArrays {
    String AnswerId;
    String EntryId;
    String FormId;
    String TableName;
    String DestColumnName;
    String TransactionId;
    String queType;
    String Answers;

    public String getAnswerId() {
        return AnswerId;
    }

    public void setAnswerId(String answerId) {
        AnswerId = answerId;
    }

    public String getEntryId() {
        return EntryId;
    }

    public void setEntryId(String entryId) {
        EntryId = entryId;
    }

    public String getFormId() {
        return FormId;
    }

    public void setFormId(String formId) {
        FormId = formId;
    }

    public String getTableName() {
        return TableName;
    }

    public void setTableName(String tableName) {
        TableName = tableName;
    }

    public String getDestColumnName() {
        return DestColumnName;
    }

    public void setDestColumnName(String destColumnName) {
        DestColumnName = destColumnName;
    }

    public String getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(String transactionId) {
        TransactionId = transactionId;
    }

    public String getQueType() {
        return queType;
    }

    public void setQueType(String queType) {
        this.queType = queType;
    }

    public String getAnswers() {
        return Answers;
    }

    public void setAnswers(String answers) {
        Answers = answers;
    }
}
