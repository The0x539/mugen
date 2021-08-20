
import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
	id("myaa.subkt")
}

subs {
	readProperties("sub.properties")
	episodes(getList("episodes"))

	fun Task.splitEpNum(): Pair<String, String> {
		val (season, ep) = episode.split("x")
		return Pair(season, ep)
	}

	val mks by task<Mux> {
		val (season, ep) = splitEpNum()

		from("$season/$ep.ass")

		attach("fonts", "fonts/$episode") {
			includeExtensions("ttf", "otf")
		}

		onFaux(ErrorMode.FAIL)

		out("$season/mux/$ep.mks")
	}

	val mkv by task<Mux> {
		val (season, ep) = splitEpNum()

		from("$season/raw/$ep.mkv") {
			tracks {
				include(track.type != TrackType.SUBTITLES)
			}
		}

		from(mks.item())

		out("$season/mux/$ep.mkv")
	}
}
