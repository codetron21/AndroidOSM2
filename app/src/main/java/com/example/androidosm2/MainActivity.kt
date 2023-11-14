package com.example.androidosm2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Track
import io.ticofab.androidgpxparser.parser.domain.TrackPoint
import io.ticofab.androidgpxparser.parser.domain.TrackSegment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var osm:MapView
    private var gpxTrackPoints: List<TrackPoint>? = null
    private var trackPointsIterator: Iterator<TrackPoint>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences(packageName,MODE_PRIVATE))

        setContentView(R.layout.activity_main)

        osm = findViewById(R.id.osm)

        osm.run {
            osm.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            osm.controller.setCenter(GeoPoint(-7.3899999,110.3702778))
            osm.controller.setZoom(16.0)
            osm.invalidate()

            if (openGpxFile()) {
                drawPolyline()
            }
        }

    }

    // Uses all the Points and creates Polyline and adds it to map
    private fun drawPolyline() {
        val gpxTrackPoints = this@MainActivity.gpxTrackPoints ?: return

        val line = Polyline(osm)
        val pts: MutableList<GeoPoint> = ArrayList()
        for (i in gpxTrackPoints.indices) {
            val lat: Double = gpxTrackPoints[i].latitude
            val lon: Double = gpxTrackPoints[i].longitude
            pts.add(GeoPoint(lat, lon))
        }
        Log.d("Points", "Successfully Loaded " + gpxTrackPoints.size + " Points")
        line.setPoints(pts)
        osm.overlayManager.add(line)
    }


    // Opens the gpx file and Loads all Points into an iterator
    private fun openGpxFile(): Boolean {
        val parser = GPXParser()
        val gpxFilename = "sample.gpx"
        val gpxParsed = try {
            parser.parse(assets.open(gpxFilename))
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
            return false
        }

        // We support playback of first segment of first track only
        if (gpxParsed != null) {
            val tracks: List<Track> = gpxParsed.tracks
            val trackSegments: List<TrackSegment>
            if (tracks.isNotEmpty()) {
                trackSegments = tracks[0].trackSegments
                if (trackSegments.isNotEmpty()) {
                    gpxTrackPoints = trackSegments[0].trackPoints
                    // Empty GPX file
                    if (gpxTrackPoints?.size == 0) {
                        return false
                    }
                    trackPointsIterator = gpxTrackPoints?.iterator()
                    return true
                }
            }
        }
        return false
    }
}