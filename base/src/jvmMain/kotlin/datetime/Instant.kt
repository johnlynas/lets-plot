package jetbrains.datalore.base.datetime

class Instant @JvmOverloads constructor(val timeSinceEpoch: Long = System.currentTimeMillis()) : Comparable<Instant> {

    fun add(duration: Duration): Instant {
        return Instant(timeSinceEpoch + duration.duration)
    }

    fun sub(duration: Duration): Instant {
        return Instant(timeSinceEpoch - duration.duration)
    }

    fun to(instant: Instant): Duration {
        return Duration(instant.timeSinceEpoch - timeSinceEpoch)
    }

    override fun compareTo(o: Instant): Int {
        val delta = timeSinceEpoch - o.timeSinceEpoch
        return if (delta > 0) {
            1
        } else if (delta == 0L) {
            0
        } else {
            -1
        }
    }

    override fun hashCode(): Int {
        return timeSinceEpoch.toInt()
    }

    override fun toString(): String {
        return "" + timeSinceEpoch
    }

    override fun equals(obj: Any?): Boolean {
        return if (obj !is Instant) false else timeSinceEpoch == obj.timeSinceEpoch

    }
}
