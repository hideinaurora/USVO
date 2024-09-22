package org.example.dto;

public class TokenDTO {

    private Long accountId;
    private Integer roleId;

    public TokenDTO() {
    }


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "{" +
                "\"accountId\":" + accountId +
                ", \"roleId\":" + roleId +
                '}';
    }
}
