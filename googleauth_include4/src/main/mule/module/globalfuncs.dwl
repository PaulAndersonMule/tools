 %dw 2.0
 import java!java::util::Base64
 fun encode(value:String) = { encoded: Base64::getUrlEncoder().encode( value.getBytes() ) }