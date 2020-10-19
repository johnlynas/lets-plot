#  Copyright (c) 2020. JetBrains s.r.o.
#  Use of this source code is governed by the MIT license that can be found in the LICENSE file.

from unittest import mock
import pytest

from lets_plot.geo_data.gis.geocoding_service import GeocodingService
from lets_plot.geo_data.gis.request import ExplicitRequest, PayloadKind, LevelKind, RequestBuilder, RequestKind, \
    RegionQuery
from lets_plot.geo_data.gis.response import Answer, FeatureBuilder, GeoPoint
from lets_plot.geo_data.regions import _coerce_resolution, _parse_resolution, Regions, Resolution, DF_ID, DF_FOUND_NAME, \
    DF_REQUEST
from lets_plot.plot import ggplot, geom_polygon
from .geo_data import make_region, make_success_response, get_map_data_meta, features_to_queries, features_to_answers

USA_REQUEST = 'united states'
USA_NAME = 'USA'
USA_ID = '1'
USA_HIGHLIGHTS = ['united states', 'united states of america']

RUSSIA_REQUEST = 'rus'
RUSSIA_NAME = 'Russian Federation'
RUSSIA_ID = '2'
RUSSIA_HIGHLIGHTS = ['rus']

RF_REQUEST = 'rf'
RF_NAME = RUSSIA_NAME
RF_ID = RUSSIA_ID

RESOLUTION = 12


def assert_region_df(region_object: FeatureBuilder, df, index=0):
    assert region_object.name == df[DF_REQUEST][index]
    assert region_object.id == df[DF_ID][index]
    assert region_object.name == df[DF_FOUND_NAME][index]


class TestMapRegions:

    def setup(self):
        self.foo_id = 'foo_id'
        self.foo_query = 'foo'
        self.foo_name = 'Foo'
        self.foo: FeatureBuilder = FeatureBuilder().set_query(self.foo_query).set_id(self.foo_id).set_name(self.foo_name)

        self.bar_id = 'bar_id'
        self.bar_query = 'bar'
        self.bar_name = 'Bar'
        self.bar: FeatureBuilder = FeatureBuilder().set_query(self.bar_query).set_id(self.bar_id).set_name(self.bar_name)

        self.baz_id = 'baz_id'
        self.baz_query = 'baz'
        self.baz_name = 'Baz'
        self.baz: FeatureBuilder = FeatureBuilder().set_query(self.baz_query).set_id(self.baz_id).set_name(self.baz_name)

    @mock.patch.object(GeocodingService, 'do_request')
    def test_boundaries(self, mock_request):
        try:
            self.make_regions().boundaries(resolution=RESOLUTION)
        except ValueError:
            pass  # response doesn't contain proper feature with ids - ignore

        mock_request.assert_called_with(
            ExplicitRequest(
                requested_payload=[PayloadKind.boundaries],
                ids=[USA_ID, RUSSIA_ID],
                resolution=_coerce_resolution(RESOLUTION)
            )
        )

    @pytest.mark.parametrize('str,expected', [
        pytest.param('city', Resolution.city_medium),
        pytest.param('county', Resolution.county_medium),
        pytest.param('state', Resolution.state_medium),
        pytest.param('country', Resolution.country_medium),
        pytest.param('world', Resolution.world_medium),
        pytest.param('city_high', Resolution.city_high)
    ])
    def test_parse_resolution(self, str, expected):
        assert expected == _parse_resolution(str)

    @mock.patch.object(GeocodingService, 'do_request')
    def test_limits(self, mock_request):
        try:
            self.make_regions().limits()
        except ValueError:
            pass  # response doesn't contain proper feature with ids - ignore

        mock_request.assert_called_with(
            ExplicitRequest(
                requested_payload=[PayloadKind.limits],
                ids=[USA_ID, RUSSIA_ID]
            )
        )

    @mock.patch.object(GeocodingService, 'do_request')
    def test_centroids(self, mock_request):
        try:
            self.make_regions().centroids()
        except ValueError:
            pass  # response doesn't contain proper feature with ids - ignore

        mock_request.assert_called_with(
            ExplicitRequest(
                requested_payload=[PayloadKind.centroids],
                ids=[USA_ID, RUSSIA_ID]
            )
        )

    def test_to_dataframe(self):
        df = Regions(
            level_kind=LevelKind.city,
            queries=[RegionQuery(request='FOO'), RegionQuery(request='BAR')],
            answers=features_to_answers([self.foo.build_geocoded(), self.bar.build_geocoded()])
        ).to_data_frame()

        assert ['FOO', 'BAR'] == df[DF_REQUEST].tolist()

    def test_as_list(self):
        regions = Regions(
            level_kind=LevelKind.city,
            queries=features_to_queries([self.foo.build_geocoded(), self.bar.build_geocoded()]),
            answers=features_to_answers([self.foo.build_geocoded(), self.bar.build_geocoded()])
        ).as_list()

        assert 2 == len(regions)

        assert_region_df(self.foo, regions[0].to_data_frame())
        assert_region_df(self.bar, regions[1].to_data_frame())

    @mock.patch.object(GeocodingService, 'do_request')
    def test_exploding_answers_to_data_frame_take_request_from_feature_name(self, mock_request):
        foo_id = '123'
        foo_name = 'foo'

        bar_id = '456'
        bar_name = 'bar'
        geocoding_result = Regions(
            level_kind=LevelKind.city,
            queries=[RegionQuery(request=None)],
            answers=[
                Answer([
                    FeatureBuilder().set_id(foo_id).set_name(foo_name).build_geocoded(),
                    FeatureBuilder().set_id(bar_id).set_name(bar_name).build_geocoded()
                ])
            ]
        )

        mock_request.return_value = make_success_response() \
            .set_geocoded_features(
            [
                FeatureBuilder().set_id(foo_id).set_query(foo_id).set_name(foo_name).set_centroid(
                    GeoPoint(0, 1)).build_geocoded(),
                FeatureBuilder().set_id(bar_id).set_query(bar_id).set_name(bar_name).set_centroid(
                    GeoPoint(2, 3)).build_geocoded(),
            ]
        ).build()

        df = geocoding_result.centroids()

        mock_request.assert_called_with(
            RequestBuilder() \
                .set_request_kind(RequestKind.explicit)
                .set_ids([foo_id, bar_id]) \
                .set_requested_payload([PayloadKind.centroids]) \
                .build()
        )

        assert foo_name == df[DF_REQUEST][0]
        assert bar_name == df[DF_REQUEST][1]

    @mock.patch.object(GeocodingService, 'do_request')
    def test_direct_answers_take_request_from_query(self, mock_request):

        geocoding_result = Regions(
            level_kind=LevelKind.city,
            queries=[
                RegionQuery(request='fooo'),
                RegionQuery(request='barr'),
                RegionQuery(request='bazz'),
            ],
            answers=[
                Answer([self.foo.set_query('').build_geocoded()]),
                Answer([self.bar.set_query('').build_geocoded()]),
                Answer([self.baz.set_query('').build_geocoded()]),
            ]
        )

        mock_request.return_value = make_success_response() \
            .set_geocoded_features(
            [
                self.foo.set_query(self.bar_id).set_centroid(GeoPoint(0, 1)).build_geocoded(),
                self.bar.set_query(self.foo_id).set_centroid(GeoPoint(0, 1)).build_geocoded(),
                self.baz.set_query(self.baz_id).set_centroid(GeoPoint(0, 1)).build_geocoded(),
            ]
        ).build()

        df = geocoding_result.centroids()

        mock_request.assert_called_with(
            RequestBuilder() \
                .set_request_kind(RequestKind.explicit) \
                .set_ids([self.foo.id, self.bar.id, self.baz.id]) \
                .set_requested_payload([PayloadKind.centroids]) \
                .build()
        )

        assert ['fooo', 'barr', 'bazz'] == df[DF_REQUEST].tolist()

    @mock.patch.object(GeocodingService, 'do_request')
    def test_df_rows_duplication_should_be_processed_correctly(self, mock_request):
        foo_id = '123'
        foo_name = 'foo'

        bar_id = '234'
        bar_name = 'bar'

        geocoding_result = Regions(
            level_kind=LevelKind.city,
            queries=[RegionQuery('foo'), RegionQuery('bar'), RegionQuery('foo')],
            answers=[
                Answer([self.foo.build_geocoded()]),
                Answer([self.bar.build_geocoded()]),
                Answer([self.foo.build_geocoded()])
            ]
        )

        mock_request.return_value = make_success_response() \
            .set_answers(
            [
                Answer([self.foo.set_query(foo_id).set_centroid(GeoPoint(0, 1)).build_geocoded()]),
                Answer([self.bar.set_query(bar_id).set_centroid(GeoPoint(0, 1)).build_geocoded()])
            ]
        ).build()

        df = geocoding_result.centroids()

        mock_request.assert_called_with(
            RequestBuilder() \
                .set_request_kind(RequestKind.explicit) \
                .set_ids([self.foo.id, self.bar.id]) \
                .set_requested_payload([PayloadKind.centroids]) \
                .build()
        )

        assert ['foo', 'bar', 'foo'] == df[DF_REQUEST].tolist()

    # python invokes geocoding functions when Regions objects detected in map
    # changed from previous version, where client invoked these functions
    @mock.patch.object(GeocodingService, 'do_request')
    def test_plot_should_have_geometries_when_regions_in_map_parameter(self, mock_request):

        mock_request.return_value = make_success_response() \
            .set_geocoded_features(
            [
                FeatureBuilder() \
                    .set_id(USA_ID) \
                    .set_name(USA_NAME) \
                    .set_boundary(GeoPoint(0, 1))
                    .build_geocoded(),
                FeatureBuilder() \
                    .set_id(RUSSIA_ID) \
                    .set_name(RUSSIA_NAME) \
                    .set_boundary(GeoPoint(0, 1))
                    .build_geocoded()

            ]
        ).build()

        plotSpec = ggplot() + geom_polygon(map=self.make_regions())

        # previous behaviour
        # expected_map_data_meta = {
        #    'georeference': {}
        # }

        expected_map_data_meta = {
            'geodataframe': {'geometry': 'geometry'}
        }

        assert expected_map_data_meta == get_map_data_meta(plotSpec, 0)

    def make_regions(self) -> Regions:
        usa = FeatureBuilder() \
            .set_name(USA_NAME) \
            .set_id(USA_ID) \
            .set_highlights(USA_HIGHLIGHTS) \
            .build_geocoded()

        russia = FeatureBuilder() \
            .set_name(RUSSIA_NAME) \
            .set_id(RUSSIA_ID) \
            .set_highlights(RUSSIA_HIGHLIGHTS) \
            .build_geocoded()

        regions = Regions(
            level_kind=LevelKind.country,
            queries=features_to_queries([usa, russia]),
            answers=features_to_answers([usa, russia])
        )
        return regions
