/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package wcg.peer;

import wcg.Block;
import wcg.Wcg;
import wcg.WcgException;
import wcg.util.Convert;
import wcg.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class ProcessBlock extends PeerServlet.PeerRequestHandler {

    static final ProcessBlock instance = new ProcessBlock();

    private ProcessBlock() {}

    @Override
    JSONStreamAware processRequest(final JSONObject request, final Peer peer) {
        String previousBlockId = (String)request.get("previousBlock");
        Block lastBlock = Wcg.getBlockchain().getLastBlock();
        if (lastBlock.getStringId().equals(previousBlockId) ||
                (Convert.parseUnsignedLong(previousBlockId) == lastBlock.getPreviousBlockId()
                        && lastBlock.getTimestamp() > Convert.parseLong(request.get("timestamp")))) {
            Peers.peersService.submit(() -> {
                try {
                    Wcg.getBlockchainProcessor().processPeerBlock(request);
                } catch (WcgException | RuntimeException e) {
                    if (peer != null) {
                        peer.blacklist(e);
                    }
                }
            });
        }
        return JSON.emptyJSON;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }

}
