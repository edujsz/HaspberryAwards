package com.eduardo.raspberryawards.service.impl;

import com.eduardo.raspberryawards.dto.MovieDTO;
import com.eduardo.raspberryawards.dto.WinnerYearDTO;
import com.eduardo.raspberryawards.exception.MovieIsWinnerException;
import com.eduardo.raspberryawards.exception.MovieNotFoundException;
import com.eduardo.raspberryawards.model.Movie;
import com.eduardo.raspberryawards.model.Producer;
import com.eduardo.raspberryawards.model.Studio;
import com.eduardo.raspberryawards.repository.MovieRepository;
import com.eduardo.raspberryawards.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Collection<MovieDTO> findByYear(Integer year) {
        return this.buildProductDTO(movieRepository.findByYear(year));
    }

    private Collection<MovieDTO> buildProductDTO(Collection<Movie> movies){
        Collection<MovieDTO> movieDTOS = new ArrayList<>();

        for (Movie movie: movies) {
            movieDTOS.add(new MovieDTO(movie.getId(), movie.getYear(),
                    movie.getTitle(), this.buildProducersList(movie),
                    this.buildStudiosList(movie), movie.getWinner()));
        }
        return movieDTOS;
    }

    private List<String> buildProducersList(Movie movie){
        List<String> producers = new ArrayList<>();
        if(movie.getProducers() != null) {
            producers = movie.getProducers()
                    .stream()
                    .map(Producer::getName)
                    .collect(Collectors.toList());
        }
        return producers;
    }

    private List<String> buildStudiosList(Movie movie){
        List<String> studios = new ArrayList<>();
        if(movie.getStudios() != null) {
            studios = movie.getStudios()
                    .stream()
                    .map(Studio::getName)
                    .collect(Collectors.toList());
        }
        return studios;
    }

    @Override
    public Iterable<Movie> findAll() {
        return this.movieRepository.findAll();
    }

    @Override
    public List<WinnerYearDTO> findTop2WinnerYears(){
        Movie movie = new Movie();
        return movieRepository.findTop2WinnerYears(PageRequest.of(0,2));
    }

    @Override
    public void delete(Long id) throws MovieNotFoundException, MovieIsWinnerException {
        Optional<Movie> movie = this.movieRepository.findById(id);

        validateMovie(id, movie);

        this.movieRepository.delete(movie.get());

    }

    @Override
    public Movie save(Movie movie) {
        return this.movieRepository.save(movie);
    }

    private void validateMovie(Long id, Optional<Movie> movie) throws MovieNotFoundException, MovieIsWinnerException {
        movie.orElseThrow(() -> new MovieNotFoundException("Filme não localizado com o ID:" + id.toString()));

        if(movie.get().getWinner()){
            throw new MovieIsWinnerException("Filme não pode ser excluído pois é um vencedor");
        }
    }
}