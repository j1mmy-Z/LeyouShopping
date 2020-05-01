package com.leyou.domain;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "tb_spec_param")
public class SpecParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    @Column(name = "`numeric`")
    private boolean numeric;
    private String unit;
    @Column(name = "generic")
    private boolean generic;
    @Column(name = "searching")
    private boolean searching;
    private String segments;


}
