package de.sonumina.zp;

public interface ZFINVisitor
{
	/**
	 * This method is called for any entry. Not that the entry
	 * is recycled.
	 * 
	 * @param entry
	 * @return
	 */
	boolean visit(ZFINEntry entry);
}
