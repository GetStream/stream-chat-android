package io.getstream.logging.android.file

import io.getstream.logging.android.file.StreamLogFileManager.ClearManager
import io.getstream.logging.android.file.StreamLogFileManager.ShareManager

public object StreamLogFileManager {

    private var shareManager: ShareManager = ShareManager { }
    private var clearManager: ClearManager = ClearManager { }

    public fun init(shareManager: ShareManager, clearManager: ClearManager) {
        this.shareManager = shareManager
        this.clearManager = clearManager
    }

    public fun share() {
        shareManager.share()
    }

    public fun clear() {
        clearManager.clear()
    }

    public fun interface ShareManager {
        public fun share()
    }

    public fun interface ClearManager {
        public fun clear()
    }
}
