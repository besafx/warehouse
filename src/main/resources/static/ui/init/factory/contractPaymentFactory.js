app.factory("ContractPaymentService",
    ['$http', '$log', function ($http, $log) {
        return {
            findOne: function (id) {
                return $http.get("/api/contractPayment/findOne/" + id).then(function (response) {
                    return response.data;
                });
            },
            create: function (contractPayment) {
                return $http.post("/api/contractPayment/create", contractPayment).then(function (response) {
                    return response.data;
                });
            },
            remove: function (id) {
                return $http.delete("/api/contractPayment/delete/" + id);
            },
            findByContract: function (id) {
                return $http.get("/api/contractPayment/findByContract/" + id).then(function (response) {
                    return response.data;
                });
            }
        };
    }]);