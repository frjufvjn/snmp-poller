import java.util.Set;
import java.util.Map.Entry;

public class tcagntClient {
  /*
  THROTTLE_BY_NUMS
    - YN : CHANNEL 다운횟수로 한번만 알람을 보낼지 여부
    - THRESHOLD : 알람 임계치 채널 갯수
    - KEYWORD : 채널알람인지 여부를 판단할 키워드
    - CHANNEL_LIMIT : 총 채널 갯수
    - CLEAR_KEYWORD : 해제 키워드 정의
    - CLEAR_PREFIX : 해제 로그 키워드일 경우, 알람메세지에 해제알람인지 명확히 구분하기 위해 Prefix를 정의한 값 (텔레캡스 어드민 UI에서도 동일하게 적용되어야 함. / critlog.ini는 포함시키지 않음.)
  */
  private static boolean isThrottleByNums = false;
  private static int throttleByNumsThreshold = 15;
  private static String throttleByNumsKeyword = "";
  private static int throttleByNumsChannelLimit = 30;
  private static String throttleByNumsCleanKeyword = "";
  private static String throttleByNumsClearPrefix = "";

  // <채널번호,클리어여부>
  private static ConcurrentHashMap<String,Boolean> throttleByNumsInfo = new ConcurrentHashMap<String,Boolean>();
  
  // 생성자
  public tcagntClient {
    String throttle_by_nums_yn = getProperty("THROTTLE_BY_NUMS_YN");
    if (throttle_by_nums_yn != null && "Y".equals(throttle_by_nums_yn)) {
      isThrottleByNums = true;
    }
    
    String throttle_by_nums_threshold = getProperty("THROTTLE_BY_NUMS_THRESHOLD");
    if (throttle_by_nums_threshold != null) {
      throttleByNumsThreshold = Integer.parseInt(throttle_by_nums_threshold);
    }
    
    String throttle_by_nums_keyword = getProperty("THROTTLE_BY_NUMS_KEYWORD");
    if (throttle_by_nums_keyword != null) {
      throttleByNumsKeyword = throttle_by_nums_keyword;
    }
    
    String throttle_by_nums_channel_limit = getProperty("THROTTLE_BY_NUMS_CHANNEL_LIMIT");
    if (throttle_by_nums_channel_limit != null) {
      throttleByNumsChannelLimit = throttle_by_nums_channel_limit;
    }
    
    String throttle_by_nums_clear_keyword = getProperty("THROTTLE_BY_NUMS_CLEAR_KEYWORD");
    if (throttle_by_nums_clear_keyword != null) {
      throttleByNumsCleanKeyword = throttle_by_nums_clear_keyword;
    }
    
    String throttle_by_nums_clear_prefix = getProperty("THROTTLE_BY_NUMS_CLEAR_PREFIX");
    if (throttle_by_nums_clear_prefix != null) {
      throttleByNumsClearPrefix = throttle_by_nums_clear_prefix;
    }
    
    sysout(ALL PROPERTIES....);
    
  }
  
  class Display implements Runnable {
    
    public void run() {
      ...
      for (int j=0; j < crit_log_pattern.size(); j++) {

        String critKey = (String) crit_log_pattern.get(j);
        if ( s3.indexOf((String) critKey) >= 0 ) {
          msg = critKey + "::" + s3;
          if ( !isThrottleByNums ) {
            // #---------------------------------------------------
            // 채널 알람이 아닌 일반 
            // #---------------------------------------------------
            new Thread(new Handler(msg)).start();
          } else {
            //#-------------------------------------------------------
            // PJW 2021-02-18 VAS 채널알람 발생 제어
            //#-------------------------------------------------------
            if (s3.indexOf(throttleByNumsKeyword) != -1) {
              String[] s3Arr = s3.split(" ");
              String chanStr = "";
              
              for (int i=0; i <s3Arr.length; i++) {
                if (s3Arr[i].indexOf("[") != -1 && s3Arr[i].indexOf("]") != -1  ) {
                  String tmpStr = s3Arr[i].substring((s3Arr[i].indexOf("[") + 1), s3Arr[i].indexOf("]"));
                  String _chanStr = tmpStr.replace("[","").replace("[","");
                  try {
                    int chanNum = Integer.parseInt(_chanStr);
                    if ( chanNum <= throttleByNumsChannelLimit ) {
                      chanStr = _chanStr;
                    }
                    break;
                    
                  } catch(NumberFormatException e) {
                    continue;
                  }
                }
              }
              
              if (!"".equals(chanStr)) {
                System.out.println("VAS CHANNEL EVT...");
                if (s3.indexOf(throttleByNumsClearKeyword) != -1) {
                  throttleByNumsInfo.put(chanStr, true);
                } else {
                  throttleByNumsInfo.put(chanStr, false);
                }
                
                int currentFailChannelCnt = 0; // 현재 Fail 상태인 채널 갯수
                int currentClearChannelCnt = 0; // 현재 Clear된 상태인 채널 갯수
                Set<Entry<String,Boolean>> entry = throttleByNumsInfo.entrySet();
                for (Entry<String,Boolean> element : entry) {
                  if (!element.getValue()) {
                    currentFailChannelCnt++;
                  } else {
                    currentClearChannelCnt++;
                  }
                }
                System.out.println("FAIL CNT:" + currentFailChannelCnt + " CLEAR CNT:" + currentClearChannelCnt);
                
                if ( currentFailChannelCnt >= throttleByNumsThreshold ) {
                  new Thread(new Handler(msg)).start();
                }
                
                if ( currentClearChannelCnt >= throttleByNumsThreshold ) {
                  // 해제알람인지 인지 시키기 위해서 정의함. 텔레캡스 어드민에서도 prefix를 동일하게 등록시켜줘야함.
                  final String chearPrefix = throttleByNumsClearPrefix;
                  new Thread(new Handler(chearPrefix + msg)).start();
                }
              }
              
            } else {
              // #---------------------------------------------------
              // 채널 알람이 아닌 일반 
              // #---------------------------------------------------
              new Thread(new Handler(msg)).start();
            }
          }
        }
      }
    }
  }
}
