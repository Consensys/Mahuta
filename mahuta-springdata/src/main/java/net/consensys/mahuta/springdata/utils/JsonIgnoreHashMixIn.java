package net.consensys.mahuta.springdata.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class JsonIgnoreHashMixIn {

    @JsonIgnore abstract String getHash(); 
}
