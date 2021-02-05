/*
    pbx data access
    1. value_id get
    2. get mib_id (select mib_id from t_mib where mib_group = 'avCmListStation';)
    3. insert t_value_call
    4. insert t_value
    5. update t_mib set last_value_id, last_updated, data_gathered where mib_id;
*/
public class SnmpDataAccessVerticle extends Ab..Ver {
    logger
    
    start {
        vertx.eventBus().<JsonObject>consumer("snmp.data.access", this::dataReceived);
    }
    
    dataReceived {
        recvData = msg.body()
        
        deviceId
        
        BigDecimal valueId = null;
        
        // mib_id get
        // value_id make get
        // oid 정보조회   select oid_name, oid from t_oid where mib_id = 
        // oid 매핑
        
    }
}
