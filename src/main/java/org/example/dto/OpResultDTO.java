package org.example.dto;

public class OpResultDTO {
    private Long longResult;
    private Object objResult;

    public OpResultDTO() {
    }

    public OpResultDTO(Long longResult, Object objResult) {
        this.longResult = longResult;
        this.objResult = objResult;
    }
    public Long getLongResult() {
        return longResult;
    }

    public void setLongResult(Long longResult) {
        this.longResult = longResult;
    }

    public Object getObjResult() {
        return objResult;
    }

    public void setObjResult(Object objResult) {
        this.objResult = objResult;
    }
}
