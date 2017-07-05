package de.charite.zpgen;

/**
 * For several annotations that are normal we generate the abnormal counterpart. For this we
 * sometimes have to correct the PATO modifier used by the annotator. E.g. "normal amount" has to be
 * replace with amount, because the "normal"-tag already indicates the fact that this is normal<br>
 * <br>
 * An example line is:
 * <code>ZDB-GENE-030131-6223	100001615	51684	sufu					ZFA:0001086	muscle pioneer	PATO:0002050	normal amount	normal						
</code>
 * 
 * @author Sebastian Koehler
 *
 */
public class EntryCorrector {

  private ZFINEntry entry;

  public EntryCorrector(ZFINEntry entry) {
    this.entry = entry;
  }

  public ZFINEntry getCorrectedEntry() {
    if (!entry.isAbnormal) {
      if (entry.patoID.equals("PATO:0002050")) { // normal amount
        entry.patoID = "PATO:0000070";
        entry.patoName = "amount";
      } else if (entry.patoID.equals("PATO:0001905")) { // has normal numbers of parts of type
        entry.patoID = "PATO:0001555";
        entry.patoName = "has number of";
      } else if (entry.patoID.equals("PATO:0000461")) { // normal
        entry.patoID = "PATO:0000001";
        entry.patoName = "quality";
      }
    }
    return entry;
  }

}
