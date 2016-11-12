package controllers.singlePlayer.mendiola;

/**
 * Created by julian on 12/11/16.
 */
public class SHash {

    public String hash;

    @Override
    public boolean equals(Object sHash){
        return this.hash.equals(((SHash)sHash).hash);
    }

    @Override
    public String toString(){
        return this.hash;
    }

    @Override
    public int hashCode() {
        return this.hash.hashCode();
    }

    public String ignore(String toIgnore){
        return this.hash.replaceAll(toIgnore, String.valueOf(Consts.OBS_ITYPE_DN));
    }
}
